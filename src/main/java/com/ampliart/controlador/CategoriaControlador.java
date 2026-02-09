package com.ampliart.controlador;

import com.ampliart.dominio.Categoria;
import com.ampliart.servico.CategoriaServico;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaControlador {

    private final CategoriaServico categoriaServico;

    public CategoriaControlador(CategoriaServico categoriaServico) {
        this.categoriaServico = categoriaServico;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaServico.listar());
        model.addAttribute("categoria", new Categoria());
        return "categorias/lista";
    }

    @PostMapping
    public String criar(@Valid Categoria categoria,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaServico.listar());
            return "categorias/lista";
        }
        categoriaServico.salvar(categoria);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria cadastrada");
        return "redirect:/categorias";
    }
}
