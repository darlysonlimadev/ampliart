package com.ampliart.controlador;

import com.ampliart.dominio.*;
import com.ampliart.repo.OrcamentoRepositorio;
import com.ampliart.servico.CategoriaServico;
import com.ampliart.servico.OrcamentoServico;
import com.ampliart.servico.ProdutoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/orcamentos")
public class OrcamentoControlador {

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
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        try {
            com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new com.lowagie.text.Paragraph("Ampliart"));
            document.add(new com.lowagie.text.Paragraph("Orcamento: " + orcamento.getId()));
            document.add(new com.lowagie.text.Paragraph("Data: " + orcamento.getDataCadastro()));
            document.add(new com.lowagie.text.Paragraph("Cliente: " + orcamento.getClienteNome() + " - " + orcamento.getClienteTelefone()));
            if (orcamento.getClienteEmail() != null) {
                document.add(new com.lowagie.text.Paragraph("Email: " + orcamento.getClienteEmail()));
            }
            document.add(new com.lowagie.text.Paragraph("Status: " + orcamento.getStatus()));

            document.add(new com.lowagie.text.Paragraph(" "));
            com.lowagie.text.pdf.PdfPTable tabela = new com.lowagie.text.pdf.PdfPTable(4);
            tabela.setWidthPercentage(100);
            tabela.addCell("Produto");
            tabela.addCell("Qtd");
            tabela.addCell("Preco unit");
            tabela.addCell("Subtotal");
            for (OrcamentoItem item : orcamento.getItens()) {
                tabela.addCell(item.getProduto().getNome());
                tabela.addCell(String.valueOf(item.getQuantidade()));
                tabela.addCell(item.getPrecoUnitario().toString());
                tabela.addCell(item.getSubtotal().toString());
            }
            document.add(tabela);

            document.add(new com.lowagie.text.Paragraph(" "));
            document.add(new com.lowagie.text.Paragraph("Total bruto: " + orcamento.getTotalBruto()));
            if (orcamento.getPercentualAjuste() != null && orcamento.getTipoAjuste() != null) {
                String sinal = orcamento.getTipoAjuste() == TipoAjuste.desconto ? "-" : "+";
                document.add(new com.lowagie.text.Paragraph("Ajuste: " + sinal + orcamento.getPercentualAjuste() + "%"));
                document.add(new com.lowagie.text.Paragraph("Valor do ajuste: " + orcamento.getValorAjuste()));
            }
            document.add(new com.lowagie.text.Paragraph("Total final: " + orcamento.getTotalFinal()));
            if (orcamento.getDataConclusao() != null) {
                document.add(new com.lowagie.text.Paragraph("Concluido em: " + orcamento.getDataConclusao()));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao gerar PDF", ex);
        } finally {
            document.close();
        }
        return baos.toByteArray();
    }
}
