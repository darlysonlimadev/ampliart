package com.ampliart.servico;

import com.ampliart.dominio.Orcamento;
import com.ampliart.dominio.OrcamentoItem;
import com.ampliart.dominio.Produto;
import com.ampliart.dominio.StatusOrcamento;
import com.ampliart.repo.OrcamentoItemRepositorio;
import com.ampliart.repo.OrcamentoRepositorio;
import com.ampliart.repo.ProdutoRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class DashboardServico {

    private static final List<String> MESES_ABREVIADOS = List.of(
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    );
    private static final DateTimeFormatter ROTULO_DIA = DateTimeFormatter.ofPattern("dd/MM");

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

    public static class SerieGraficoVendas {
        private final List<String> rotulos;
        private final List<BigDecimal> receitas;
        private final List<BigDecimal> gastos;
        private final List<BigDecimal> lucros;

        public SerieGraficoVendas(List<String> rotulos,
                                  List<BigDecimal> receitas,
                                  List<BigDecimal> gastos,
                                  List<BigDecimal> lucros) {
            this.rotulos = rotulos;
            this.receitas = receitas;
            this.gastos = gastos;
            this.lucros = lucros;
        }

        public List<String> getRotulos() {
            return rotulos;
        }

        public List<BigDecimal> getReceitas() {
            return receitas;
        }

        public List<BigDecimal> getGastos() {
            return gastos;
        }

        public List<BigDecimal> getLucros() {
            return lucros;
        }
    }

    public static class ProdutoMaisVendido {
        private final String nome;
        private final long quantidadeVendida;
        private final BigDecimal receita;

        public ProdutoMaisVendido(String nome, long quantidadeVendida, BigDecimal receita) {
            this.nome = nome;
            this.quantidadeVendida = quantidadeVendida;
            this.receita = receita;
        }

        public String getNome() {
            return nome;
        }

        public long getQuantidadeVendida() {
            return quantidadeVendida;
        }

        public BigDecimal getReceita() {
            return receita;
        }
    }

    public static class MesDestaqueVenda {
        private final String mes;
        private final BigDecimal receita;

        public MesDestaqueVenda(String mes, BigDecimal receita) {
            this.mes = mes;
            this.receita = receita;
        }

        public String getMes() {
            return mes;
        }

        public BigDecimal getReceita() {
            return receita;
        }
    }

    public static class AnaliseVendas {
        private final String periodoSelecionado;
        private final boolean periodoPersonalizado;
        private final LocalDate referencia;
        private final LocalDate dataInicio;
        private final LocalDate dataFim;
        private final BigDecimal receitaTotal;
        private final BigDecimal gastoTotal;
        private final BigDecimal lucroTotal;
        private final long vendasConcluidas;
        private final BigDecimal ticketMedio;
        private final BigDecimal margemLucro;
        private final SerieGraficoVendas serieGrafico;
        private final List<ProdutoMaisVendido> produtosMaisVendidos;
        private final List<MesDestaqueVenda> melhoresMeses;

        public AnaliseVendas(String periodoSelecionado,
                             boolean periodoPersonalizado,
                             LocalDate referencia,
                             LocalDate dataInicio,
                             LocalDate dataFim,
                             BigDecimal receitaTotal,
                             BigDecimal gastoTotal,
                             BigDecimal lucroTotal,
                             long vendasConcluidas,
                             BigDecimal ticketMedio,
                             BigDecimal margemLucro,
                             SerieGraficoVendas serieGrafico,
                             List<ProdutoMaisVendido> produtosMaisVendidos,
                             List<MesDestaqueVenda> melhoresMeses) {
            this.periodoSelecionado = periodoSelecionado;
            this.periodoPersonalizado = periodoPersonalizado;
            this.referencia = referencia;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.receitaTotal = receitaTotal;
            this.gastoTotal = gastoTotal;
            this.lucroTotal = lucroTotal;
            this.vendasConcluidas = vendasConcluidas;
            this.ticketMedio = ticketMedio;
            this.margemLucro = margemLucro;
            this.serieGrafico = serieGrafico;
            this.produtosMaisVendidos = produtosMaisVendidos;
            this.melhoresMeses = melhoresMeses;
        }

        public String getPeriodoSelecionado() {
            return periodoSelecionado;
        }

        public boolean isPeriodoPersonalizado() {
            return periodoPersonalizado;
        }

        public LocalDate getReferencia() {
            return referencia;
        }

        public LocalDate getDataInicio() {
            return dataInicio;
        }

        public LocalDate getDataFim() {
            return dataFim;
        }

        public BigDecimal getReceitaTotal() {
            return receitaTotal;
        }

        public BigDecimal getGastoTotal() {
            return gastoTotal;
        }

        public BigDecimal getLucroTotal() {
            return lucroTotal;
        }

        public long getVendasConcluidas() {
            return vendasConcluidas;
        }

        public BigDecimal getTicketMedio() {
            return ticketMedio;
        }

        public BigDecimal getMargemLucro() {
            return margemLucro;
        }

        public SerieGraficoVendas getSerieGrafico() {
            return serieGrafico;
        }

        public List<ProdutoMaisVendido> getProdutosMaisVendidos() {
            return produtosMaisVendidos;
        }

        public List<MesDestaqueVenda> getMelhoresMeses() {
            return melhoresMeses;
        }
    }

    private enum TipoPeriodo {
        dia,
        semana,
        mes,
        ano,
        personalizado
    }

    private static class FaixaPeriodo {
        private final TipoPeriodo tipoPeriodo;
        private final boolean faixaPersonalizada;
        private final LocalDate referencia;
        private final LocalDate dataInicio;
        private final LocalDate dataFim;
        private final LocalDateTime inicio;
        private final LocalDateTime fimExclusivo;
        private final List<String> rotulos;

        private FaixaPeriodo(TipoPeriodo tipoPeriodo,
                             boolean faixaPersonalizada,
                             LocalDate referencia,
                             LocalDate dataInicio,
                             LocalDate dataFim,
                             LocalDateTime inicio,
                             LocalDateTime fimExclusivo,
                             List<String> rotulos) {
            this.tipoPeriodo = tipoPeriodo;
            this.faixaPersonalizada = faixaPersonalizada;
            this.referencia = referencia;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.inicio = inicio;
            this.fimExclusivo = fimExclusivo;
            this.rotulos = rotulos;
        }
    }

    private static class AcumuladorProduto {
        private final String nome;
        private long quantidadeVendida;
        private BigDecimal receita = BigDecimal.ZERO;

        private AcumuladorProduto(String nome) {
            this.nome = nome;
        }
    }

    private final OrcamentoRepositorio orcamentoRepositorio;
    private final OrcamentoItemRepositorio orcamentoItemRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    public DashboardServico(OrcamentoRepositorio orcamentoRepositorio,
                            OrcamentoItemRepositorio orcamentoItemRepositorio,
                            ProdutoRepositorio produtoRepositorio) {
        this.orcamentoRepositorio = orcamentoRepositorio;
        this.orcamentoItemRepositorio = orcamentoItemRepositorio;
        this.produtoRepositorio = produtoRepositorio;
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

    public BigDecimal carregarValorEmEstoque() {
        return produtoRepositorio.findAll().stream()
                .filter(Produto::getAtivo)
                .map(produto -> produto.getPrecoVenda().multiply(BigDecimal.valueOf(produto.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public long carregarQuantidadeEstoqueBaixo(int limite) {
        int limiteAplicado = limite > 0 ? limite : 10;
        return produtoRepositorio.findAll().stream()
                .filter(Produto::getAtivo)
                .filter(produto -> produto.getQuantidadeEstoque() <= limiteAplicado)
                .count();
    }

    public List<Produto> listarProdutosEstoqueBaixo(int limite) {
        int limiteAplicado = limite > 0 ? limite : 10;
        return produtoRepositorio.findAll().stream()
                .filter(Produto::getAtivo)
                .filter(produto -> produto.getQuantidadeEstoque() <= limiteAplicado)
                .sorted(Comparator.comparing(Produto::getQuantidadeEstoque)
                        .thenComparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public AnaliseVendas carregarAnaliseVendas(String periodo, LocalDate referencia) {
        return carregarAnaliseVendas(periodo, referencia, null, null);
    }

    public AnaliseVendas carregarAnaliseVendas(String periodo,
                                               LocalDate referencia,
                                               LocalDate dataInicio,
                                               LocalDate dataFim) {
        TipoPeriodo periodoSelecionado = resolverTipoPeriodo(periodo);
        FaixaPeriodo faixaPeriodo = montarFaixaPeriodo(periodoSelecionado, referencia, dataInicio, dataFim);
        LocalDateTime fimConsulta = faixaPeriodo.fimExclusivo.minusNanos(1);

        List<Orcamento> orcamentosPeriodo = orcamentoRepositorio.buscarConcluidosPeriodo(
                StatusOrcamento.venda_concluida,
                faixaPeriodo.inicio,
                fimConsulta
        );
        List<OrcamentoItem> itensPeriodo = orcamentoItemRepositorio.buscarItensConcluidosPeriodo(
                faixaPeriodo.inicio,
                fimConsulta
        );

        BigDecimal receitaTotal = somarReceita(orcamentosPeriodo);
        BigDecimal gastoTotal = somarGasto(itensPeriodo);
        BigDecimal lucroTotal = normalizarValor(receitaTotal.subtract(gastoTotal));
        long vendasConcluidas = orcamentosPeriodo.size();
        BigDecimal ticketMedio = vendasConcluidas == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : receitaTotal.divide(BigDecimal.valueOf(vendasConcluidas), 2, RoundingMode.HALF_UP);
        BigDecimal margemLucro = receitaTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : lucroTotal.multiply(BigDecimal.valueOf(100)).divide(receitaTotal, 2, RoundingMode.HALF_UP);

        return new AnaliseVendas(
                periodoSelecionado.name(),
                faixaPeriodo.faixaPersonalizada,
                faixaPeriodo.referencia,
                faixaPeriodo.dataInicio,
                faixaPeriodo.dataFim,
                receitaTotal,
                gastoTotal,
                lucroTotal,
                vendasConcluidas,
                ticketMedio,
                margemLucro,
                montarSerieGrafico(faixaPeriodo, orcamentosPeriodo, itensPeriodo),
                calcularProdutosMaisVendidos(itensPeriodo, 10),
                calcularMelhoresMeses(8)
        );
    }

    private FaixaPeriodo montarFaixaPeriodo(TipoPeriodo tipoPeriodo,
                                            LocalDate referencia,
                                            LocalDate dataInicio,
                                            LocalDate dataFim) {
        LocalDate referenciaAplicada = Optional.ofNullable(referencia).orElse(LocalDate.now());

        if (dataInicio != null || dataFim != null) {
            LocalDate inicioData = Optional.ofNullable(dataInicio).orElse(dataFim);
            LocalDate fimData = Optional.ofNullable(dataFim).orElse(dataInicio);
            if (inicioData == null || fimData == null) {
                return montarFaixaPadrao(tipoPeriodo, referenciaAplicada);
            }
            if (fimData.isBefore(inicioData)) {
                LocalDate troca = inicioData;
                inicioData = fimData;
                fimData = troca;
            }
            LocalDateTime inicio = inicioData.atStartOfDay();
            LocalDateTime fimExclusivo = fimData.plusDays(1).atStartOfDay();
            List<String> rotulos = montarRotulosPersonalizados(inicioData, fimData);
            return new FaixaPeriodo(
                    TipoPeriodo.personalizado,
                    true,
                    referenciaAplicada,
                    inicioData,
                    fimData,
                    inicio,
                    fimExclusivo,
                    rotulos
            );
        }

        return montarFaixaPadrao(tipoPeriodo, referenciaAplicada);
    }

    private FaixaPeriodo montarFaixaPadrao(TipoPeriodo tipoPeriodo, LocalDate referenciaAplicada) {

        return switch (tipoPeriodo) {
            case dia -> {
                LocalDateTime inicio = referenciaAplicada.atStartOfDay();
                LocalDateTime fimExclusivo = referenciaAplicada.plusDays(1).atStartOfDay();
                List<String> rotulos = IntStream.range(0, 24)
                        .mapToObj(hora -> String.format(Locale.ROOT, "%02dh", hora))
                        .toList();
                yield new FaixaPeriodo(tipoPeriodo, false, referenciaAplicada, referenciaAplicada, referenciaAplicada, inicio, fimExclusivo, rotulos);
            }
            case semana -> {
                LocalDate inicioData = referenciaAplicada.with(DayOfWeek.MONDAY);
                LocalDate fimData = inicioData.plusDays(6);
                LocalDateTime inicio = inicioData.atStartOfDay();
                LocalDateTime fimExclusivo = fimData.plusDays(1).atStartOfDay();
                yield new FaixaPeriodo(tipoPeriodo, false, referenciaAplicada, inicioData, fimData, inicio, fimExclusivo,
                        List.of("Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"));
            }
            case mes -> {
                LocalDate inicioData = referenciaAplicada.withDayOfMonth(1);
                LocalDate fimData = referenciaAplicada.withDayOfMonth(referenciaAplicada.lengthOfMonth());
                LocalDateTime inicio = inicioData.atStartOfDay();
                LocalDateTime fimExclusivo = fimData.plusDays(1).atStartOfDay();
                List<String> rotulos = IntStream.rangeClosed(1, fimData.getDayOfMonth())
                        .mapToObj(dia -> String.format(Locale.ROOT, "%02d", dia))
                        .toList();
                yield new FaixaPeriodo(tipoPeriodo, false, referenciaAplicada, inicioData, fimData, inicio, fimExclusivo, rotulos);
            }
            case ano -> {
                LocalDate inicioData = referenciaAplicada.withDayOfYear(1);
                LocalDate fimData = referenciaAplicada.withDayOfYear(referenciaAplicada.lengthOfYear());
                LocalDateTime inicio = inicioData.atStartOfDay();
                LocalDateTime fimExclusivo = fimData.plusDays(1).atStartOfDay();
                yield new FaixaPeriodo(tipoPeriodo, false, referenciaAplicada, inicioData, fimData, inicio, fimExclusivo, MESES_ABREVIADOS);
            }
            case personalizado -> throw new IllegalStateException("Periodo personalizado precisa de intervalo de datas");
        };
    }

    private List<String> montarRotulosPersonalizados(LocalDate inicioData, LocalDate fimData) {
        int totalDias = (int) ChronoUnit.DAYS.between(inicioData, fimData) + 1;
        return IntStream.range(0, totalDias)
                .mapToObj(indice -> inicioData.plusDays(indice).format(ROTULO_DIA))
                .toList();
    }

    private TipoPeriodo resolverTipoPeriodo(String periodo) {
        try {
            TipoPeriodo tipoPeriodo = TipoPeriodo.valueOf(Optional.ofNullable(periodo).orElse("mes").toLowerCase(Locale.ROOT));
            return tipoPeriodo == TipoPeriodo.personalizado ? TipoPeriodo.mes : tipoPeriodo;
        } catch (IllegalArgumentException ex) {
            return TipoPeriodo.mes;
        }
    }

    private SerieGraficoVendas montarSerieGrafico(FaixaPeriodo faixaPeriodo,
                                                  List<Orcamento> orcamentosPeriodo,
                                                  List<OrcamentoItem> itensPeriodo) {
        int tamanho = faixaPeriodo.rotulos.size();
        BigDecimal[] receitas = inicializarSerie(tamanho);
        BigDecimal[] gastos = inicializarSerie(tamanho);

        for (Orcamento orcamento : orcamentosPeriodo) {
            int indice = identificarIndiceFaixa(faixaPeriodo, orcamento.getDataConclusao());
            if (indice < 0 || indice >= tamanho) {
                continue;
            }
            receitas[indice] = receitas[indice].add(orcamento.getTotalFinal());
        }

        for (OrcamentoItem item : itensPeriodo) {
            if (item.getProduto() == null || item.getProduto().getPrecoCompra() == null) {
                continue;
            }
            LocalDateTime dataConclusao = item.getOrcamento() != null ? item.getOrcamento().getDataConclusao() : null;
            int indice = identificarIndiceFaixa(faixaPeriodo, dataConclusao);
            if (indice < 0 || indice >= tamanho) {
                continue;
            }
            BigDecimal gastoItem = item.getProduto().getPrecoCompra().multiply(BigDecimal.valueOf(item.getQuantidade()));
            gastos[indice] = gastos[indice].add(gastoItem);
        }

        List<BigDecimal> serieReceita = new ArrayList<>(tamanho);
        List<BigDecimal> serieGasto = new ArrayList<>(tamanho);
        List<BigDecimal> serieLucro = new ArrayList<>(tamanho);
        for (int i = 0; i < tamanho; i++) {
            BigDecimal receita = normalizarValor(receitas[i]);
            BigDecimal gasto = normalizarValor(gastos[i]);
            serieReceita.add(receita);
            serieGasto.add(gasto);
            serieLucro.add(normalizarValor(receita.subtract(gasto)));
        }

        return new SerieGraficoVendas(faixaPeriodo.rotulos, serieReceita, serieGasto, serieLucro);
    }

    private BigDecimal[] inicializarSerie(int tamanho) {
        BigDecimal[] valores = new BigDecimal[tamanho];
        for (int i = 0; i < tamanho; i++) {
            valores[i] = BigDecimal.ZERO;
        }
        return valores;
    }

    private int identificarIndiceFaixa(FaixaPeriodo faixaPeriodo, LocalDateTime dataHora) {
        if (dataHora == null) {
            return -1;
        }
        return switch (faixaPeriodo.tipoPeriodo) {
            case dia -> dataHora.getHour();
            case semana -> dataHora.getDayOfWeek().getValue() - 1;
            case mes -> dataHora.getDayOfMonth() - 1;
            case ano -> dataHora.getMonthValue() - 1;
            case personalizado -> (int) ChronoUnit.DAYS.between(faixaPeriodo.dataInicio, dataHora.toLocalDate());
        };
    }

    private List<ProdutoMaisVendido> calcularProdutosMaisVendidos(List<OrcamentoItem> itensPeriodo, int limite) {
        Map<Long, AcumuladorProduto> acumulado = new HashMap<>();

        for (OrcamentoItem item : itensPeriodo) {
            if (item.getProduto() == null || item.getProduto().getId() == null) {
                continue;
            }
            long produtoId = item.getProduto().getId();
            AcumuladorProduto produto = acumulado.computeIfAbsent(produtoId, id -> new AcumuladorProduto(item.getProduto().getNome()));
            produto.quantidadeVendida += item.getQuantidade();
            produto.receita = produto.receita.add(item.getSubtotal());
        }

        return acumulado.values().stream()
                .sorted(Comparator.comparingLong((AcumuladorProduto item) -> item.quantidadeVendida).reversed()
                        .thenComparing((AcumuladorProduto item) -> item.receita, Comparator.reverseOrder()))
                .limit(limite)
                .map(item -> new ProdutoMaisVendido(item.nome, item.quantidadeVendida, normalizarValor(item.receita)))
                .toList();
    }

    private List<MesDestaqueVenda> calcularMelhoresMeses(int limite) {
        Map<YearMonth, BigDecimal> receitaPorMes = new HashMap<>();
        List<Orcamento> historicoConcluido = orcamentoRepositorio.buscarConcluidos(StatusOrcamento.venda_concluida);

        for (Orcamento orcamento : historicoConcluido) {
            if (orcamento.getDataConclusao() == null) {
                continue;
            }
            YearMonth anoMes = YearMonth.from(orcamento.getDataConclusao());
            receitaPorMes.merge(anoMes, orcamento.getTotalFinal(), BigDecimal::add);
        }

        return receitaPorMes.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, BigDecimal>comparingByValue().reversed())
                .limit(limite)
                .map(item -> new MesDestaqueVenda(formatarMes(item.getKey()), normalizarValor(item.getValue())))
                .toList();
    }

    private String formatarMes(YearMonth anoMes) {
        return MESES_ABREVIADOS.get(anoMes.getMonthValue() - 1) + "/" + anoMes.getYear();
    }

    private BigDecimal somarReceita(List<Orcamento> orcamentos) {
        return normalizarValor(orcamentos.stream()
                .map(Orcamento::getTotalFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal somarGasto(List<OrcamentoItem> itens) {
        return normalizarValor(itens.stream()
                .filter(item -> item.getProduto() != null && item.getProduto().getPrecoCompra() != null)
                .map(item -> item.getProduto().getPrecoCompra().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal normalizarValor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private IndicadorPeriodo calcularPeriodo(String titulo, LocalDateTime inicio, LocalDateTime fimExclusivo) {
        LocalDateTime fimConsulta = fimExclusivo.minusNanos(1);
        List<Orcamento> orcamentos = orcamentoRepositorio.buscarConcluidosPeriodo(StatusOrcamento.venda_concluida, inicio, fimConsulta);
        BigDecimal receita = orcamentos.stream()
                .map(Orcamento::getTotalFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<OrcamentoItem> itens = orcamentoItemRepositorio.buscarItensConcluidosPeriodo(inicio, fimConsulta);
        BigDecimal gasto = itens.stream()
                .map(item -> item.getProduto().getPrecoCompra().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal lucro = receita.subtract(gasto).setScale(2, RoundingMode.HALF_UP);
        return new IndicadorPeriodo(titulo, receita, gasto, lucro);
    }
}
