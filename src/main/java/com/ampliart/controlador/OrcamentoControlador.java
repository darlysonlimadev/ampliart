package com.ampliart.controlador;

import com.ampliart.dominio.Orcamento;
import com.ampliart.dominio.OrcamentoItem;
import com.ampliart.dominio.Produto;
import com.ampliart.dominio.StatusOrcamento;
import com.ampliart.dominio.TipoAjuste;
import com.ampliart.repo.OrcamentoRepositorio;
import com.ampliart.servico.CategoriaServico;
import com.ampliart.servico.OrcamentoServico;
import com.ampliart.servico.ProdutoServico;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/orcamentos")
public class OrcamentoControlador {

    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_BR);

    private static final Color COR_TEXTO = new Color(42, 42, 42);
    private static final Color COR_FUNDO_PASTEL = new Color(247, 238, 232);
    private static final Color COR_FUNDO_ALTERNADO = new Color(250, 250, 250);
    private static final Color COR_BORDA_SUAVE = new Color(214, 214, 214);

    private final OrcamentoRepositorio orcamentoRepositorio;
    private final OrcamentoServico orcamentoServico;
    private final ProdutoServico produtoServico;
    private final CategoriaServico categoriaServico;

    public OrcamentoControlador(OrcamentoRepositorio orcamentoRepositorio,
                                OrcamentoServico orcamentoServico,
                                ProdutoServico produtoServico,
                                CategoriaServico categoriaServico) {
        this.orcamentoRepositorio = orcamentoRepositorio;
        this.orcamentoServico = orcamentoServico;
        this.produtoServico = produtoServico;
        this.categoriaServico = categoriaServico;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusOrcamento status,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                         Model model) {
        List<Orcamento> orcamentos;
        if (dataInicio != null && dataFim != null) {
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.plusDays(1).atStartOfDay();
            if (status != null) {
                orcamentos = orcamentoRepositorio.buscarPorStatusEDataCadastro(status, inicio, fim);
            } else {
                orcamentos = orcamentoRepositorio.buscarPorDataCadastro(inicio, fim);
            }
        } else if (status != null) {
            orcamentos = orcamentoRepositorio.findByStatus(status);
        } else {
            orcamentos = orcamentoRepositorio.findAll();
        }
        model.addAttribute("orcamentos", orcamentos);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("statusPossiveis", StatusOrcamento.values());
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        return "orcamentos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("orcamento", new Orcamento());
        return "orcamentos/novo";
    }

    @PostMapping("/novo")
    public String criar(@Valid @ModelAttribute("orcamento") Orcamento orcamento,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "orcamentos/novo";
        }
        Orcamento criado = orcamentoServico.criar(orcamento);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Orcamento criado");
        return "redirect:/orcamentos/" + criado.getId();
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id,
                           @RequestParam(required = false) String nomeProduto,
                           @RequestParam(required = false) Long categoriaId,
                           Model model) {
        Orcamento orcamento = orcamentoServico.buscarPorId(id);
        model.addAttribute("orcamento", orcamento);
        model.addAttribute("statusPossiveis", StatusOrcamento.values());
        model.addAttribute("tiposAjuste", TipoAjuste.values());
        model.addAttribute("categorias", categoriaServico.listar());
        model.addAttribute("nomeProduto", nomeProduto);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("produtosDisponiveis", produtoServico.listarPorNomeECategoria(nomeProduto, categoriaId));
        return "orcamentos/detalhe";
    }

    @PostMapping("/{id}/itens/adicionar")
    public String adicionarItem(@PathVariable Long id,
                                @RequestParam String codigoProduto,
                                @RequestParam(required = false) Integer quantidade,
                                RedirectAttributes redirectAttributes) {
        try {
            orcamentoServico.adicionarItemPorCodigo(id, codigoProduto, quantidade);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Item adicionado");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/orcamentos/" + id;
    }

    @PostMapping("/{id}/itens/adicionar-por-id")
    public String adicionarItemPorId(@PathVariable Long id,
                                     @RequestParam Long produtoId,
                                     @RequestParam(required = false) Integer quantidade,
                                     RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoServico.buscarPorId(produtoId);
            orcamentoServico.adicionarItemPorCodigo(id, produto.getCodigo(), quantidade);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Item adicionado");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/orcamentos/" + id;
    }

    @PostMapping("/{id}/itens/{itemId}/atualizar")
    public String atualizarItem(@PathVariable Long id,
                                @PathVariable Long itemId,
                                @RequestParam Integer quantidade,
                                @RequestParam BigDecimal precoUnitario,
                                RedirectAttributes redirectAttributes) {
        try {
            orcamentoServico.atualizarItem(id, itemId, quantidade, precoUnitario);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Item atualizado");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/orcamentos/" + id;
    }

    @PostMapping("/{id}/itens/{itemId}/remover")
    public String removerItem(@PathVariable Long id,
                              @PathVariable Long itemId,
                              RedirectAttributes redirectAttributes) {
        try {
            orcamentoServico.removerItem(id, itemId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Item removido");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/orcamentos/" + id;
    }

    @PostMapping("/{id}/ajuste")
    public String aplicarAjuste(@PathVariable Long id,
                                @RequestParam TipoAjuste tipoAjuste,
                                @RequestParam BigDecimal percentual,
                                RedirectAttributes redirectAttributes) {
        try {
            orcamentoServico.aplicarAjuste(id, tipoAjuste, percentual);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Ajuste aplicado");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/orcamentos/" + id;
    }

    @PostMapping("/{id}/ajuste/remover")
    public String removerAjuste(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            orcamentoServico.removerAjuste(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Ajuste removido");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/orcamentos/" + id;
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam StatusOrcamento novoStatus,
                                RedirectAttributes redirectAttributes) {
        try {
            orcamentoServico.alterarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Status atualizado");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/orcamentos/" + id;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable Long id) {
        Orcamento orcamento = orcamentoServico.buscarPorId(id);
        byte[] pdf = gerarPdf(orcamento);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orcamento-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private byte[] gerarPdf(Orcamento orcamento) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 34, 34, 30, 30);
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COR_TEXTO);
            Font fonteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, COR_TEXTO);
            Font fonteRotulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COR_TEXTO);
            Font fonteCorpo = FontFactory.getFont(FontFactory.HELVETICA, 10, COR_TEXTO);
            Font fonteCorpoNegrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COR_TEXTO);
            Font fonteTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COR_TEXTO);

            document.add(montarCabecalho(orcamento, fonteTitulo, fonteSubtitulo, fonteCorpo, fonteCorpoNegrito));
            document.add(montarBlocoCliente(orcamento, fonteRotulo, fonteCorpo));
            document.add(montarTabelaItens(orcamento, fonteRotulo, fonteCorpo));
            document.add(montarBlocoTotais(orcamento, fonteCorpo, fonteCorpoNegrito, fonteTotal));

            Paragraph rodape = new Paragraph("Documento gerado automaticamente por Ampliart.", fonteSubtitulo);
            rodape.setSpacingBefore(10f);
            rodape.setAlignment(Element.ALIGN_LEFT);
            document.add(rodape);
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao gerar PDF", ex);
        } finally {
            document.close();
        }
        return baos.toByteArray();
    }

    private PdfPTable montarCabecalho(Orcamento orcamento,
                                      Font fonteTitulo,
                                      Font fonteSubtitulo,
                                      Font fonteCorpo,
                                      Font fonteCorpoNegrito) throws Exception {
        PdfPTable cabecalho = new PdfPTable(2);
        cabecalho.setWidthPercentage(100);
        cabecalho.setWidths(new float[]{2.7f, 1.3f});
        cabecalho.setSpacingAfter(12f);

        PdfPCell celulaEsquerda = new PdfPCell();
        celulaEsquerda.setBorder(Rectangle.NO_BORDER);
        celulaEsquerda.addElement(new Paragraph("Ampliart", fonteTitulo));
        Paragraph subtitulo = new Paragraph("Orcamento comercial", fonteSubtitulo);
        subtitulo.setSpacingBefore(2f);
        celulaEsquerda.addElement(subtitulo);

        PdfPCell celulaDireita = new PdfPCell();
        celulaDireita.setBorder(Rectangle.NO_BORDER);
        celulaDireita.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaDireita.addElement(paragrafoAlinhado("ORCAMENTO #" + orcamento.getId(), fonteCorpoNegrito, Element.ALIGN_RIGHT));
        celulaDireita.addElement(paragrafoAlinhado("Data: " + formatarDataHora(orcamento.getDataCadastro()), fonteCorpo, Element.ALIGN_RIGHT));
        celulaDireita.addElement(paragrafoAlinhado("Status: " + formatarStatus(orcamento.getStatus()), fonteCorpo, Element.ALIGN_RIGHT));

        cabecalho.addCell(celulaEsquerda);
        cabecalho.addCell(celulaDireita);
        return cabecalho;
    }

    private PdfPTable montarBlocoCliente(Orcamento orcamento,
                                         Font fonteRotulo,
                                         Font fonteCorpo) {
        PdfPTable tabela = new PdfPTable(1);
        tabela.setWidthPercentage(100);
        tabela.setSpacingAfter(12f);

        PdfPCell cabecalho = new PdfPCell(new Phrase("DADOS DO CLIENTE", fonteRotulo));
        cabecalho.setBackgroundColor(COR_FUNDO_PASTEL);
        cabecalho.setPadding(8f);
        cabecalho.setBorderColor(COR_BORDA_SUAVE);

        PdfPCell conteudo = new PdfPCell();
        conteudo.setPadding(9f);
        conteudo.setBorderColor(COR_BORDA_SUAVE);
        conteudo.addElement(new Paragraph(orcamento.getClienteNome(), fonteCorpo));
        conteudo.addElement(new Paragraph("Telefone: " + orcamento.getClienteTelefone(), fonteCorpo));
        if (orcamento.getClienteEmail() != null && !orcamento.getClienteEmail().isBlank()) {
            conteudo.addElement(new Paragraph("Email: " + orcamento.getClienteEmail(), fonteCorpo));
        }

        tabela.addCell(cabecalho);
        tabela.addCell(conteudo);
        return tabela;
    }

    private PdfPTable montarTabelaItens(Orcamento orcamento,
                                        Font fonteRotulo,
                                        Font fonteCorpo) throws Exception {
        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{5f, 1.1f, 2f, 2f});
        tabela.setSpacingAfter(12f);

        tabela.addCell(criarCelulaCabecalho("Produto", fonteRotulo));
        tabela.addCell(criarCelulaCabecalho("Qtd", fonteRotulo));
        tabela.addCell(criarCelulaCabecalho("Preco unit.", fonteRotulo));
        tabela.addCell(criarCelulaCabecalho("Subtotal", fonteRotulo));

        if (orcamento.getItens().isEmpty()) {
            PdfPCell vazio = criarCelulaConteudo("Sem itens no orcamento", fonteCorpo, Element.ALIGN_CENTER, Color.WHITE);
            vazio.setColspan(4);
            tabela.addCell(vazio);
            return tabela;
        }

        for (int i = 0; i < orcamento.getItens().size(); i++) {
            OrcamentoItem item = orcamento.getItens().get(i);
            Color fundoLinha = i % 2 == 0 ? Color.WHITE : COR_FUNDO_ALTERNADO;
            tabela.addCell(criarCelulaConteudo(item.getProduto().getNome(), fonteCorpo, Element.ALIGN_LEFT, fundoLinha));
            tabela.addCell(criarCelulaConteudo(String.valueOf(item.getQuantidade()), fonteCorpo, Element.ALIGN_CENTER, fundoLinha));
            tabela.addCell(criarCelulaConteudo(formatarMoeda(item.getPrecoUnitario()), fonteCorpo, Element.ALIGN_RIGHT, fundoLinha));
            tabela.addCell(criarCelulaConteudo(formatarMoeda(item.getSubtotal()), fonteCorpo, Element.ALIGN_RIGHT, fundoLinha));
        }

        return tabela;
    }

    private PdfPTable montarBlocoTotais(Orcamento orcamento,
                                        Font fonteCorpo,
                                        Font fonteCorpoNegrito,
                                        Font fonteTotal) throws Exception {
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(48);
        tabela.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.setWidths(new float[]{2f, 1.3f});

        tabela.addCell(criarCelulaConteudo("Total bruto", fonteCorpo, Element.ALIGN_LEFT, COR_FUNDO_ALTERNADO));
        tabela.addCell(criarCelulaConteudo(formatarMoeda(orcamento.getTotalBruto()), fonteCorpoNegrito, Element.ALIGN_RIGHT, COR_FUNDO_ALTERNADO));

        String descricaoAjuste = "Sem ajuste";
        String valorAjuste = formatarMoeda(BigDecimal.ZERO);
        if (orcamento.getTipoAjuste() != null && orcamento.getPercentualAjuste() != null) {
            String sinal = orcamento.getTipoAjuste() == TipoAjuste.desconto ? "-" : "+";
            descricaoAjuste = (orcamento.getTipoAjuste() == TipoAjuste.desconto ? "Desconto" : "Acrescimo")
                    + " (" + sinal + orcamento.getPercentualAjuste().stripTrailingZeros().toPlainString() + "%)";
            valorAjuste = formatarMoeda(orcamento.getValorAjuste());
        }

        tabela.addCell(criarCelulaConteudo(descricaoAjuste, fonteCorpo, Element.ALIGN_LEFT, Color.WHITE));
        tabela.addCell(criarCelulaConteudo(valorAjuste, fonteCorpoNegrito, Element.ALIGN_RIGHT, Color.WHITE));

        tabela.addCell(criarCelulaConteudo("Total final", fonteTotal, Element.ALIGN_LEFT, COR_FUNDO_PASTEL));
        tabela.addCell(criarCelulaConteudo(formatarMoeda(orcamento.getTotalFinal()), fonteTotal, Element.ALIGN_RIGHT, COR_FUNDO_PASTEL));

        return tabela;
    }

    private PdfPCell criarCelulaCabecalho(String texto, Font fonte) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBackgroundColor(COR_FUNDO_PASTEL);
        celula.setBorderColor(COR_BORDA_SUAVE);
        celula.setPadding(7f);
        return celula;
    }

    private PdfPCell criarCelulaConteudo(String texto,
                                         Font fonte,
                                         int alinhamento,
                                         Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setHorizontalAlignment(alinhamento);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setBorderColor(COR_BORDA_SUAVE);
        celula.setBackgroundColor(fundo);
        celula.setPadding(6f);
        return celula;
    }

    private Paragraph paragrafoAlinhado(String texto, Font fonte, int alinhamento) {
        Paragraph p = new Paragraph(texto, fonte);
        p.setAlignment(alinhamento);
        return p;
    }

    private String formatarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            return "-";
        }
        return dataHora.format(FORMATO_DATA_HORA);
    }

    private String formatarStatus(StatusOrcamento status) {
        if (status == null) {
            return "-";
        }
        return status.getLabel();
    }

    private String formatarMoeda(BigDecimal valor) {
        BigDecimal valorAplicado = valor != null ? valor : BigDecimal.ZERO;
        NumberFormat formatador = NumberFormat.getCurrencyInstance(LOCALE_BR);
        return formatador.format(valorAplicado);
    }
}
