package com.ampliart.repo;

import com.ampliart.dominio.OrcamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrcamentoItemRepositorio extends JpaRepository<OrcamentoItem, Long> {

    @Query("select i from OrcamentoItem i join i.orcamento o where o.status = 'venda_concluida' and o.dataConclusao between :inicio and :fim")
    List<OrcamentoItem> buscarItensConcluidosPeriodo(@Param("inicio") LocalDateTime inicio,
                                                     @Param("fim") LocalDateTime fim);
}
