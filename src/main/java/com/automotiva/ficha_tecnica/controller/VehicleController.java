package com.automotiva.ficha_tecnica.controller;

import com.automotiva.ficha_tecnica.exception.BadRequestException;
import com.automotiva.ficha_tecnica.service.VehicleService;
import com.automotiva.ficha_tecnica.service.dto.ComparacaoRequest;
import com.automotiva.ficha_tecnica.service.dto.ComparacaoResponse;
import com.automotiva.ficha_tecnica.service.dto.VehicleCreateRequest;
import com.automotiva.ficha_tecnica.service.dto.VehicleCrudResponse;
import com.automotiva.ficha_tecnica.service.dto.VehicleRequest;
import com.automotiva.ficha_tecnica.service.dto.VehicleResponse;
import com.automotiva.ficha_tecnica.service.dto.VehicleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
@Tag(name = "Veiculos", description = "Gerenciamento de veiculos")
@Validated
public class VehicleController {

    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @Operation(summary = "Buscar especificacoes do veiculo")
    @PostMapping("/especificacoes")
    @PreAuthorize("hasAnyRole('USER','ANALYST','ADMIN')")
    public ResponseEntity<VehicleResponse> buscarEspecificacoes(
            @Valid @RequestBody VehicleRequest request
    ) {
        VehicleResponse response = service.buscarEspecificacoes(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Comparar dois veiculos")
    @PostMapping("/comparar")
    @PreAuthorize("hasAnyRole('USER','ANALYST','ADMIN')")
    public ResponseEntity<ComparacaoResponse> comparar(
            @Valid @RequestBody ComparacaoRequest request
    ) {
        ComparacaoResponse response = service.comparar(request.veiculo1(), request.veiculo2());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar novo veiculo")
    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<VehicleCrudResponse> criar(
            @Valid @RequestBody VehicleCreateRequest request,
            UriComponentsBuilder uriBuilder
    ) throws BadRequestException {
        VehicleCrudResponse response = service.criar(request);

        URI uri = uriBuilder
                .path("/api/veiculos/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Atualizar veiculo")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<VehicleCrudResponse> atualizar(
            @PathVariable @Positive(message = "Id invalido") Long id,
            @Valid @RequestBody VehicleCreateRequest request
    ) {
        VehicleCrudResponse response = service.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar parcialmente veiculo")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<VehicleCrudResponse> atualizarParcialmente(
            @PathVariable @Positive(message = "Id invalido") Long id,
            @Valid @RequestBody VehicleUpdateRequest request
    ) {
        VehicleCrudResponse response = service.atualizarParcialmente(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deletar veiculo")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(
            @PathVariable @Positive(message = "Id invalido") Long id
    ) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar veiculos")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ANALYST','ADMIN')")
    public ResponseEntity<List<VehicleCrudResponse>> listar(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Pagina invalida") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Tamanho invalido") @Max(value = 100, message = "Tamanho maximo excedido") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleCrudResponse> response = service.listarPaginado(pageable);
        return ResponseEntity.ok(response.getContent());
    }
}
