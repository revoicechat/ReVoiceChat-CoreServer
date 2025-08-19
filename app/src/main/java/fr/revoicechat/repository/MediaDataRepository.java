package fr.revoicechat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.revoicechat.model.MediaData;

public interface MediaDataRepository extends JpaRepository<MediaData, UUID> {}
