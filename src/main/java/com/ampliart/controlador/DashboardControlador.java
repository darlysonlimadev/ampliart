package com.ampliart.controlador;

import com.ampliart.servico.DashboardServico;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardControlador {

    private final DashboardServico dashboardServico;

    public DashboardControlador(DashboardServico dashboardServico) {
        this.dashboardServico = dashboardServico;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("indicadorHoje", dashboardServico.carregarHoje());
        model.addAttribute("indicadorSemana", dashboardServico.carregarSemana());
        model.addAttribute("indicadorMes", dashboardServico.carregarMes());
        model.addAttribute("indicadorAno", dashboardServico.carregarAno());
        List<DashboardServico.IndicadorPeriodo> ultimosDias = dashboardServico.carregarUltimosDias(7);
        model.addAttribute("ultimosDias", ultimosDias);
        model.addAttribute("labelsGrafico", ultimosDias.stream()
                .map(DashboardServico.IndicadorPeriodo::getPeriodo)
                .collect(Collectors.toList()));
        model.addAttribute("receitasGrafico", ultimosDias.stream()
                .map(DashboardServico.IndicadorPeriodo::getReceita)
                .collect(Collectors.toList()));
        model.addAttribute("lucrosGrafico", ultimosDias.stream()
                .map(DashboardServico.IndicadorPeriodo::getLucro)
                .collect(Collectors.toList()));
        return "dashboard/index";
    }
}
