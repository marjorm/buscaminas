package com.bachillerato.buscaminas.services;

import com.bachillerato.buscaminas.repositories.PartidaRepository;
import com.bachillerato.buscaminas.repositories.RankingProjection;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RankingService {

    private final PartidaRepository partidaRepository;

    public RankingService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    public List<RankingProjection> obtenerRanking() {
        return partidaRepository.obtenerRanking();
    }
}