package com.automotiva.ficha_tecnica.service;

import com.automotiva.ficha_tecnica.audit.AuditService;
import com.automotiva.ficha_tecnica.entity.Atributo;
import com.automotiva.ficha_tecnica.entity.Especificacao;
import com.automotiva.ficha_tecnica.entity.Veiculo;
import com.automotiva.ficha_tecnica.exception.BadRequestException;
import com.automotiva.ficha_tecnica.exception.NotFoundException;
import com.automotiva.ficha_tecnica.repository.AtributoRepository;
import com.automotiva.ficha_tecnica.repository.EspecificacaoRepository;
import com.automotiva.ficha_tecnica.repository.VehicleRepository;
import com.automotiva.ficha_tecnica.security.SecurityInputValidator;
import com.automotiva.ficha_tecnica.service.dto.ComparacaoResponse;
import com.automotiva.ficha_tecnica.service.dto.VehicleCreateRequest;
import com.automotiva.ficha_tecnica.service.dto.VehicleCrudResponse;
import com.automotiva.ficha_tecnica.service.dto.VehicleRequest;
import com.automotiva.ficha_tecnica.service.dto.VehicleResponse;
import com.automotiva.ficha_tecnica.service.dto.VehicleUpdateRequest;
import com.automotiva.ficha_tecnica.util.StringNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final EspecificacaoRepository especificacaoRepository;
    private final VehicleRepository vehicleRepository;
    private final AtributoRepository atributoRepository;
    private final SecurityInputValidator inputValidator;
    private final AuditService auditService;

    public VehicleService(
            EspecificacaoRepository especificacaoRepository,
            VehicleRepository vehicleRepository,
            AtributoRepository atributoRepository,
            SecurityInputValidator inputValidator,
            AuditService auditService
    ) {
        this.especificacaoRepository = especificacaoRepository;
        this.vehicleRepository = vehicleRepository;
        this.atributoRepository = atributoRepository;
        this.inputValidator = inputValidator;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public VehicleResponse buscarEspecificacoes(VehicleRequest request) {
        VehicleRequest sanitizedRequest = sanitizeVehicleRequest(request);

        List<String> atributos = normalizarLista(sanitizedRequest.atributos());

        List<Especificacao> specs = especificacaoRepository.buscarEspecificacoes(
                sanitizedRequest.marca(),
                sanitizedRequest.modelo(),
                sanitizedRequest.versao(),
                atributos
        );

        Map<String, String> resultado = new LinkedHashMap<>();

        for (String atributo : atributos) {
            resultado.put(atributo, "Nao disponivel");
        }

        for (Especificacao e : specs) {
            String nome = normalizar(e.getAtributo().getNome());

            String valorFormatado;

            if ("X".equalsIgnoreCase(e.getValor())) {
                valorFormatado = "Disponível";               
            } else if ("0".equals(e.getValor())) {
                valorFormatado = "Não Disponível";
            } else {
                valorFormatado = e.getValor();
            }
            resultado.put(nome, valorFormatado);
        }

        return new VehicleResponse(
                sanitizedRequest.marca(),
                sanitizedRequest.modelo(),
                sanitizedRequest.versao(),
                resultado
        );
    }

    @Transactional(readOnly = true)
    public ComparacaoResponse comparar(VehicleRequest v1, VehicleRequest v2) {
        VehicleRequest left = sanitizeVehicleRequest(v1);
        VehicleRequest right = sanitizeVehicleRequest(v2);

        List<String> atributos = normalizarLista(left.atributos());

        VehicleResponse r1 = buscarEspecificacoes(left);
        VehicleResponse r2 = buscarEspecificacoes(right);

        Map<String, Map<String, String>> resultado = new LinkedHashMap<>();

        String chaveV1 = montarNomeVeiculo(left);
        String chaveV2 = montarNomeVeiculo(right);

        for (String atributo : atributos) {
            Map<String, String> linha = new LinkedHashMap<>();

            linha.put(chaveV1, Optional.ofNullable(r1.especificacoes().get(atributo)).orElse("Nao disponivel"));
            linha.put(chaveV2, Optional.ofNullable(r2.especificacoes().get(atributo)).orElse("Nao disponivel"));

            resultado.put(atributo, linha);
        }

        return new ComparacaoResponse(resultado);
    }

    @Transactional
    public VehicleCrudResponse criar(VehicleCreateRequest request) throws BadRequestException {
        VehicleCreateRequest sanitizedRequest = sanitizeCreateRequest(request);
        validarDuplicidade(sanitizedRequest);

        Veiculo vehicle = Veiculo.builder()
                .marca(sanitizedRequest.marca())
                .modelo(sanitizedRequest.modelo())
                .versao(sanitizedRequest.versao())
                .build();

        vehicle = vehicleRepository.save(vehicle);

        salvarEspecificacoes(vehicle, sanitizedRequest.especificacoes());

        auditService.register("CREATE_VEHICLE", "vehicle:" + vehicle.getId(), "Criacao de veiculo");
        log.info("Veiculo criado: id={}", vehicle.getId());

        return montarResponse(vehicle, sanitizedRequest.especificacoes());
    }

    @Transactional
    public VehicleCrudResponse atualizar(Long id, VehicleCreateRequest request) {
        VehicleCreateRequest sanitizedRequest = sanitizeCreateRequest(request);

        Veiculo vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado"));

        vehicle.setMarca(sanitizedRequest.marca());
        vehicle.setModelo(sanitizedRequest.modelo());
        vehicle.setVersao(sanitizedRequest.versao());

        vehicleRepository.save(vehicle);

        List<Especificacao> existentes = especificacaoRepository.findByVehicleId(id);

        Map<String, Especificacao> mapaExistentes = existentes.stream()
                .collect(Collectors.toMap(e -> normalizar(e.getAtributo().getNome()), e -> e));

        Map<String, String> especificacoes = Optional.ofNullable(sanitizedRequest.especificacoes())
                .orElse(Collections.emptyMap());

        List<Especificacao> paraSalvar = new ArrayList<>();

        for (Map.Entry<String, String> entry : especificacoes.entrySet()) {
            String nome = normalizar(entry.getKey());
            String valor = inputValidator.sanitizeSpecValue(entry.getValue());

            Especificacao existente = mapaExistentes.get(nome);

            if (existente != null) {
                existente.setValor(valor);
                paraSalvar.add(existente);
            } else {
                Atributo atributo = atributoRepository
                        .findByNomeIgnoreCase(nome)
                        .orElseThrow(() -> new BadRequestException("Atributo nao permitido: " + nome));

                Especificacao nova = Especificacao.builder()
                        .vehicle(vehicle)
                        .atributo(atributo)
                        .valor(valor)
                        .build();

                paraSalvar.add(nova);
            }
        }

        especificacaoRepository.saveAll(paraSalvar);

        auditService.register("UPDATE_VEHICLE", "vehicle:" + id, "Atualizacao completa");
        log.info("Veiculo atualizado: id={}", id);

        return montarResponse(vehicle, especificacoes);
    }

    @Transactional
    public VehicleCrudResponse atualizarParcialmente(Long id, VehicleUpdateRequest request) {
        Veiculo vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado"));

        if (request.marca() != null) {
            vehicle.setMarca(inputValidator.sanitizeText("marca", request.marca(), 60));
        }

        if (request.modelo() != null) {
            vehicle.setModelo(inputValidator.sanitizeText("modelo", request.modelo(), 60));
        }

        if (request.versao() != null) {
            vehicle.setVersao(inputValidator.sanitizeText("versao", request.versao(), 60));
        }

        if (request.especificacoes() != null) {
            atualizarEspecificacoes(vehicle, request.especificacoes());
        }

        vehicleRepository.save(vehicle);
        auditService.register("PATCH_VEHICLE", "vehicle:" + id, "Atualizacao parcial");

        return montarResponse(vehicle);
    }

    private void atualizarEspecificacoes(Veiculo vehicle, Map<String, String> especificacoes) {
        especificacoes.forEach((nomeAtributo, valor) -> {
            String nomeSeguro = inputValidator.sanitizeAttributeName(nomeAtributo);
            String valorSeguro = inputValidator.sanitizeSpecValue(valor);

            Atributo atributo = atributoRepository
                    .findByNomeIgnoreCase(nomeSeguro)
                    .orElseThrow(() -> new BadRequestException("Atributo nao permitido: " + nomeSeguro));

            Especificacao especificacao = especificacaoRepository
                    .findByVehicleAndAtributo(vehicle, atributo)
                    .orElseGet(() -> {
                        Especificacao nova = new Especificacao();
                        nova.setVehicle(vehicle);
                        nova.setAtributo(atributo);
                        return nova;
                    });

            especificacao.setValor(valorSeguro);
            especificacaoRepository.save(especificacao);
        });
    }

    private VehicleCrudResponse montarResponse(Veiculo vehicle) {
        Map<String, String> especificacoes = especificacaoRepository
                .findByVehicleId(vehicle.getId())
                .stream()
                .collect(Collectors.toMap(e -> e.getAtributo().getNome(), Especificacao::getValor));

        return new VehicleCrudResponse(
                vehicle.getId(),
                vehicle.getMarca(),
                vehicle.getModelo(),
                vehicle.getVersao(),
                especificacoes
        );
    }

    @Transactional
    public void deletar(Long id) {
        Veiculo vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado"));

        especificacaoRepository.deleteByVehicleId(id);
        vehicleRepository.delete(vehicle);

        auditService.register("DELETE_VEHICLE", "vehicle:" + id, "Remocao de veiculo");
        log.info("Veiculo deletado: id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<VehicleCrudResponse> listarPaginado(Pageable pageable) {
        if (pageable.getPageSize() >= 50) {
            auditService.register("MASS_QUERY", "vehicle:list", "Consulta com pagina grande");
        }

        return vehicleRepository.findAll(pageable)
                .map(v -> new VehicleCrudResponse(
                        v.getId(),
                        v.getMarca(),
                        v.getModelo(),
                        v.getVersao(),
                        null
                ));
    }

    private void validarDuplicidade(VehicleCreateRequest request) throws BadRequestException {
        boolean existe = vehicleRepository.existsByMarcaIgnoreCaseAndModeloIgnoreCaseAndVersaoIgnoreCase(
                request.marca(),
                request.modelo(),
                request.versao()
        );

        if (existe) {
            throw new BadRequestException("Ja existe um veiculo com essa marca/modelo/versao");
        }
    }

    private void salvarEspecificacoes(Veiculo vehicle, Map<String, String> especificacoes) {
        Map<String, String> mapa = Optional.ofNullable(especificacoes).orElse(Collections.emptyMap());

        List<Especificacao> lista = new ArrayList<>();

        for (Map.Entry<String, String> entry : mapa.entrySet()) {
            String nome = inputValidator.sanitizeAttributeName(normalizar(entry.getKey()));
            String valor = inputValidator.sanitizeSpecValue(entry.getValue());

            Atributo atributo = atributoRepository
                    .findByNomeIgnoreCase(nome)
                    .orElseThrow(() -> new BadRequestException("Atributo nao permitido: " + nome));

            Especificacao especificacao = Especificacao.builder()
                    .vehicle(vehicle)
                    .atributo(atributo)
                    .valor(valor)
                    .build();

            lista.add(especificacao);
        }

        if (!lista.isEmpty()) {
            especificacaoRepository.saveAll(lista);
        }
    }

    private VehicleCrudResponse montarResponse(Veiculo vehicle, Map<String, String> especificacoes) {
        return new VehicleCrudResponse(
                vehicle.getId(),
                vehicle.getMarca(),
                vehicle.getModelo(),
                vehicle.getVersao(),
                especificacoes
        );
    }

    private VehicleRequest sanitizeVehicleRequest(VehicleRequest request) {
        List<String> atributos = Optional.ofNullable(request.atributos())
                .orElse(Collections.emptyList())
                .stream()
                .map(inputValidator::sanitizeAttributeName)
                .toList();

        return new VehicleRequest(
                inputValidator.sanitizeText("marca", request.marca(), 60),
                inputValidator.sanitizeText("modelo", request.modelo(), 60),
                inputValidator.sanitizeText("versao", request.versao(), 60),
                atributos
        );
    }

    private VehicleCreateRequest sanitizeCreateRequest(VehicleCreateRequest request) {
        Map<String, String> specs = Optional.ofNullable(request.especificacoes())
                .orElse(Collections.emptyMap())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> inputValidator.sanitizeAttributeName(e.getKey()),
                        e -> inputValidator.sanitizeSpecValue(e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        return new VehicleCreateRequest(
                inputValidator.sanitizeText("marca", request.marca(), 60),
                inputValidator.sanitizeText("modelo", request.modelo(), 60),
                inputValidator.sanitizeText("versao", request.versao(), 60),
                specs
        );
    }

    private String normalizar(String valor) {
        return StringNormalizer.normalize(valor);
    }

    private List<String> normalizarLista(List<String> lista) {
        return Optional.ofNullable(lista)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::normalizar)
                .collect(Collectors.toList());
    }

    private String montarNomeVeiculo(VehicleRequest request) {
        return request.marca() + " " + request.modelo() + " " + request.versao();
    }
}
