// ============================================
// FILE: src/main/java/com/aguardi/shared/dto/SearchRequest.java
// Propósito: DTO genérico para búsquedas con paginación
// ============================================

package com.aguardi.ecommerce.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {

    private String searchTerm;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
