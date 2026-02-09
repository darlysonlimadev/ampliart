package com.ampliart.servico;

import com.ampliart.dominio.Orcamento;
import com.ampliart.dominio.OrcamentoItem;
import com.ampliart.dominio.StatusOrcamento;
import com.ampliart.repo.OrcamentoItemRepositorio;
import com.ampliart.repo.OrcamentoRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServico {

    public static class IndicadorPeriodo {
        private final String periodo;
        private final BigDecimal receita;
        private final BigDecimal gasto;
        private final BigDecimal lucro;

        public IndicadorPeriodo(String periodo, BigDecimal receita, BigDecimal gasto, BigDecimal lucro) {
            this.periodo = periodo;
            this.receita = receita;
            this.gasto = gasto;
            this.lucro = lucro;
        }

        public String getPeriodo() {
            return periodo;
        }

        public BigDecimal getReceita() {
            return receita;
        }

        public BigDecimal getGasto() {
            return gasto;
        }

        public BigDecimal getLucro() {
            return lucro;
        }
    }

    private final OrcamentoRepositorio orcamentoRepositorio;
    private final OrcamentoItemRepositorio orcamentoItemRepositorio;

    public DashboardServico(OrcamentoRepositorio orcamentoRepositorio,
                            OrcamentoItemRepositorio orcamentoItemRepositorio) {
        this.orcamentoRepositorio = orcamentoRepositorio;
        this.orcamentoItemRepositorio = orcamentoItemRepositorio;
    }

    public IndicadorPeriodo carregarHoje() {
        LocalDate hoje = LocalDate.now();
        return calcularPeriodo("Hoje", hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
    }

    public IndicadorPeriodo carregarSemana() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.with(DayOfWeek.MONDAY);
        LocalDate fim = inicio.plusDays(7);
        return calcularPeriodo("Semana", inicio.atStartOfDay(), fim.atStartOfDay());
    }

    public IndicadorPeriodo carregarMes() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1);
        return calcularPeriodo("Mes", inicio.atStartOfDay(), fim.atStartOfDay());
    }

    public IndicadorPeriodo carregarAno() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfYear(1);
        LocalDate fim = inicio.plusYears(1);
        return calcularPeriodo("Ano", inicio.atStartOfDay(), fim.atStartOfDay());
    }

    public List<IndicadorPeriodo> carregarUltimosDias(int dias) {
        List<IndicadorPeriodo> lista = new ArrayList<>();
        for (int i = dias - 1; i >= 0; i--) {
            LocalDate dia = LocalDate.now().minusDays(i);
            lista.add(calcularPeriodo(dia.toString(), dia.atStartOfDay(), dia.plusDays(1).atStartOfDay()));
        }
        return lista;
    }

    private IndicadorPeriodo calcularPeriodo(String titulo, LocalDateTime inicio, LocalDateTime fim) {
        List<Orcamento> orcamentos = orcamentoRepositorio.buscarConcluidosPeriodo(StatusOrcamento.venda_concluida, inicio, fim);
        BigDecimal receita = orcamentos.stream()
                .map(Orcamento::getTotalFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<OrcamentoItem> itens = orcamentoItemRepositorio.buscarItensConcluidosPeriodo(inicio, fim);
        BigDecimal gasto = itens.stream()
                .map(i -> i.getProduto().getPrecoCompra().multiply(new BigDecimal(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal lucro = receita.subtract(gasto).setScale(2, RoundingMode.HALF_UP);
        return new IndicadorPeriodo(titulo, receita, gasto, lucro);
    }
}
