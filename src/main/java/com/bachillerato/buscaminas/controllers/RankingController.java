package com.bachillerato.buscaminas.controllers;

import com.bachillerato.buscaminas.services.RankingService;
import com.bachillerato.buscaminas.repositories.RankingProjection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public List<RankingProjection> obtenerRanking() {
        return rankingService.obtenerRanking();
    }
}