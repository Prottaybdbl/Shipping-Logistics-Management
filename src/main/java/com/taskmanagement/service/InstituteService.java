package com.taskmanagement.service;

import com.taskmanagement.entity.Institute;
import com.taskmanagement.repository.InstituteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstituteService {

    private final InstituteRepository instituteRepository;

    public InstituteService(InstituteRepository instituteRepository) {
        this.instituteRepository = instituteRepository;
    }

    public List<Institute> findAll() {
        return instituteRepository.findAll();
    }

    public Optional<Institute> findById(Long id) {
        return instituteRepository.findById(id);
    }

    public Institute save(Institute institute) {
        return instituteRepository.save(institute);
    }

    public void deleteById(Long id) {
        instituteRepository.deleteById(id);
    }

    public List<Institute> findByStatus(Institute.InstituteStatus status) {
        return instituteRepository.findByStatus(status);
    }

    public boolean existsByName(String name) {
        return instituteRepository.existsByName(name);
    }

    public long countActive() {
        return instituteRepository.countActiveInstitutes();
    }
}
