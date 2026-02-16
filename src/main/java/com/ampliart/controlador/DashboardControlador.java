package com.ampliart.controlador;

import com.ampliart.servico.DashboardServico;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardControlador {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DashboardServico dashboardServico;

    public DashboardControlador(DashboardServico dashboardServico) {
        this.dashboardServico = dashboardServico;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("indicadorMes", dashboardServico.carregarMes());
        model.addAttribute("valorEmEstoque", dashboardServico.carregarValorEmEstoque());
        model.addAttribute("estoqueBaixo", dashboardServico.carregarQuantidadeEstoqueBaixo(10));
        return "dashboard/index";
    }

    @GetMapping("/estoque-baixo")
    public String estoqueBaixo(@RequestParam(defaultValue = "10") Integer limite,
                               Model model) {
        int limiteAplicado = limite != null && limite > 0 ? limite : 10;
        model.addAttribute("limite", limiteAplicado);
        model.addAttribute("totalProdutosBaixo", dashboardServico.carregarQuantidadeEstoqueBaixo(limiteAplicado));
        model.addAttribute("produtosEstoqueBaixo", dashboardServico.listarProdutosEstoqueBaixo(limiteAplicado));
        return "dashboard/estoque-baixo";
    }

    @GetMapping("/vendas")
    public String analiseVendas(@RequestParam(defaultValue = "mes") String periodo,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referencia,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                                Model model) {
        DashboardServico.AnaliseVendas analise = carregarAnalise(periodo, referencia, dataInicio, dataFim);
        model.addAttribute("analise", analise);
        model.addAttribute("periodosDisponiveis", List.of("dia", "semana", "mes", "ano"));
        model.addAttribute("melhoresMesesRotulos", analise.getMelhoresMeses().stream().map(DashboardServico.MesDestaqueVenda::getMes).toList());
        model.addAttribute("melhoresMesesReceitas", analise.getMelhoresMeses().stream().map(DashboardServico.MesDestaqueVenda::getReceita).toList());
        return "dashboard/vendas";
    }

    @GetMapping("/vendas/exportar/csv")
    public ResponseEntity<byte[]> exportarAnaliseCsv(@RequestParam(defaultValue = "mes") String periodo,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referencia,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        DashboardServico.AnaliseVendas analise = carregarAnalise(periodo, referencia, dataInicio, dataFim);
        String csv = gerarCsvAnalise(analise);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analise-vendas-" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/vendas/exportar/pdf")
    public ResponseEntity<byte[]> exportarAnalisePdf(@RequestParam(defaultValue = "mes") String periodo,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referencia,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        DashboardServico.AnaliseVendas analise = carregarAnalise(periodo, referencia, dataInicio, dataFim);
        byte[] pdf = gerarPdfAnalise(analise);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analise-vendas-" + LocalDate.now() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private DashboardServico.AnaliseVendas carregarAnalise(String periodo,
                                                           LocalDate referencia,
                                                           LocalDate dataInicio,
                                                           LocalDate dataFim) {
        return dashboardServico.carregarAnaliseVendas(periodo, referencia, dataInicio, dataFim);
    }

    private String gerarCsvAnalise(DashboardServico.AnaliseVendas analise) {
        StringBuilder csv = new StringBuilder("\uFEFF");

        adicionarLinhaCsv(csv, "Analytics de Vendas");
        adicionarLinhaCsv(csv, "Periodo", analise.getPeriodoSelecionado());
        adicionarLinhaCsv(csv, "Data inicio", formatarData(analise.getDataInicio()));
        adicionarLinhaCsv(csv, "Data fim", formatarData(analise.getDataFim()));
        adicionarLinhaCsv(csv, "Receita total", formatarMoeda(analise.getReceitaTotal()));
        adicionarLinhaCsv(csv, "Gasto total", formatarMoeda(analise.getGastoTotal()));
        adicionarLinhaCsv(csv, "Lucro total", formatarMoeda(analise.getLucroTotal()));
        adicionarLinhaCsv(csv, "Vendas concluidas", String.valueOf(analise.getVendasConcluidas()));
        adicionarLinhaCsv(csv, "Ticket medio", formatarMoeda(analise.getTicketMedio()));
        adicionarLinhaCsv(csv, "Margem", analise.getMargemLucro().setScale(2, RoundingMode.HALF_UP) + "%");

        adicionarLinhaCsv(csv, "");
        adicionarLinhaCsv(csv, "Serie do periodo");
        adicionarLinhaCsv(csv, "Rotulo", "Receita", "Gasto", "Lucro");
        for (int i = 0; i < analise.getSerieGrafico().getRotulos().size(); i++) {
            adicionarLinhaCsv(csv,
                    analise.getSerieGrafico().getRotulos().get(i),
                    formatarMoeda(analise.getSerieGrafico().getReceitas().get(i)),
                    formatarMoeda(analise.getSerieGrafico().getGastos().get(i)),
                    formatarMoeda(analise.getSerieGrafico().getLucros().get(i))
            );
        }

        adicionarLinhaCsv(csv, "");
        adicionarLinhaCsv(csv, "Produtos mais vendidos");
        adicionarLinhaCsv(csv, "Produto", "Quantidade", "Receita");
        if (analise.getProdutosMaisVendidos().isEmpty()) {
            adicionarLinhaCsv(csv, "Sem vendas concluidas neste periodo");
        } else {
            for (DashboardServico.ProdutoMaisVendido produto : analise.getProdutosMaisVendidos()) {
                adicionarLinhaCsv(csv,
                        produto.getNome(),
                        String.valueOf(produto.getQuantidadeVendida()),
                        formatarMoeda(produto.getReceita())
                );
            }
        }

        adicionarLinhaCsv(csv, "");
        adicionarLinhaCsv(csv, "Melhores meses");
        adicionarLinhaCsv(csv, "Mes", "Receita");
        if (analise.getMelhoresMeses().isEmpty()) {
            adicionarLinhaCsv(csv, "Sem historico suficiente");
        } else {
            for (DashboardServico.MesDestaqueVenda mes : analise.getMelhoresMeses()) {
                adicionarLinhaCsv(csv, mes.getMes(), formatarMoeda(mes.getReceita()));
            }
        }

        return csv.toString();
    }

    private void adicionarLinhaCsv(StringBuilder csv, String... colunas) {
        for (int i = 0; i < colunas.length; i++) {
            csv.append(escaparCsv(colunas[i]));
            if (i < colunas.length - 1) {
                csv.append(';');
            }
        }
        csv.append('\n');
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor;
        boolean precisaAspas = texto.contains(";") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r");
        if (texto.contains("\"")) {
            texto = texto.replace("\"", "\"\"");
        }
        return precisaAspas ? "\"" + texto + "\"" : texto;
    }

    private byte[] gerarPdfAnalise(DashboardServico.AnaliseVendas analise) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font corpo = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Analytics de Vendas", titulo));
            document.add(new Paragraph("Periodo: " + formatarIntervalo(analise), corpo));
            document.add(new Paragraph("Gerado em: " + formatarData(LocalDate.now()), corpo));
            document.add(new Paragraph(" "));

            PdfPTable tabelaResumo = new PdfPTable(2);
            tabelaResumo.setWidthPercentage(100);
            tabelaResumo.setSpacingAfter(12);
            tabelaResumo.addCell(criarCabecalho("Indicador"));
            tabelaResumo.addCell(criarCabecalho("Valor"));
            tabelaResumo.addCell(criarCelula("Receita total"));
            tabelaResumo.addCell(criarCelula(formatarMoeda(analise.getReceitaTotal())));
            tabelaResumo.addCell(criarCelula("Gasto total"));
            tabelaResumo.addCell(criarCelula(formatarMoeda(analise.getGastoTotal())));
            tabelaResumo.addCell(criarCelula("Lucro total"));
            tabelaResumo.addCell(criarCelula(formatarMoeda(analise.getLucroTotal())));
            tabelaResumo.addCell(criarCelula("Vendas concluidas"));
            tabelaResumo.addCell(criarCelula(String.valueOf(analise.getVendasConcluidas())));
            tabelaResumo.addCell(criarCelula("Ticket medio"));
            tabelaResumo.addCell(criarCelula(formatarMoeda(analise.getTicketMedio())));
            tabelaResumo.addCell(criarCelula("Margem"));
            tabelaResumo.addCell(criarCelula(analise.getMargemLucro().setScale(2, RoundingMode.HALF_UP) + "%"));
            document.add(tabelaResumo);

            document.add(new Paragraph("Produtos Mais Vendidos", subtitulo));
            document.add(new Paragraph(" "));
            PdfPTable tabelaProdutos = new PdfPTable(new float[]{5f, 1.5f, 2f});
            tabelaProdutos.setWidthPercentage(100);
            tabelaProdutos.setSpacingAfter(12);
            tabelaProdutos.addCell(criarCabecalho("Produto"));
            tabelaProdutos.addCell(criarCabecalho("Qtd"));
            tabelaProdutos.addCell(criarCabecalho("Receita"));
            if (analise.getProdutosMaisVendidos().isEmpty()) {
                PdfPCell vazio = criarCelula("Sem vendas concluidas neste periodo");
                vazio.setColspan(3);
                tabelaProdutos.addCell(vazio);
            } else {
                for (DashboardServico.ProdutoMaisVendido produto : analise.getProdutosMaisVendidos()) {
                    tabelaProdutos.addCell(criarCelula(produto.getNome()));
                    tabelaProdutos.addCell(criarCelula(String.valueOf(produto.getQuantidadeVendida())));
                    tabelaProdutos.addCell(criarCelula(formatarMoeda(produto.getReceita())));
                }
            }
            document.add(tabelaProdutos);

            document.add(new Paragraph("Melhores Meses de Venda", subtitulo));
            document.add(new Paragraph(" "));
            PdfPTable tabelaMeses = new PdfPTable(new float[]{3f, 2f});
            tabelaMeses.setWidthPercentage(70);
            tabelaMeses.addCell(criarCabecalho("Mes"));
            tabelaMeses.addCell(criarCabecalho("Receita"));
            if (analise.getMelhoresMeses().isEmpty()) {
                PdfPCell vazio = criarCelula("Sem historico suficiente");
                vazio.setColspan(2);
                tabelaMeses.addCell(vazio);
            } else {
                for (DashboardServico.MesDestaqueVenda mes : analise.getMelhoresMeses()) {
                    tabelaMeses.addCell(criarCelula(mes.getMes()));
                    tabelaMeses.addCell(criarCelula(formatarMoeda(mes.getReceita())));
                }
            }
            document.add(tabelaMeses);
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao gerar PDF da analise", ex);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private PdfPCell criarCabecalho(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto));
        celula.setBackgroundColor(new Color(233, 236, 243));
        celula.setPadding(6);
        return celula;
    }

    private PdfPCell criarCelula(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto));
        celula.setPadding(6);
        return celula;
    }

    private String formatarIntervalo(DashboardServico.AnaliseVendas analise) {
        return formatarData(analise.getDataInicio()) + " ate " + formatarData(analise.getDataFim());
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(FORMATO_DATA_BR) : "-";
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            return "R$ 0,00";
        }
        String numero = valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
        return "R$ " + numero;
    }
}
