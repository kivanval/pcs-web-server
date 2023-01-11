package com.example.pcswebserver.service;

import com.example.pcswebserver.data.StoreDirRepository;
import com.example.pcswebserver.data.StoreFileRepository;
import com.example.pcswebserver.data.UserRepository;
import com.example.pcswebserver.domain.StoreFile;
import com.example.pcswebserver.domain.StorePermissionType;
import com.example.pcswebserver.domain.User;
import com.example.pcswebserver.exception.StorageException;
import com.example.pcswebserver.exception.StoreDirNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreFileService {
    @Value("${store.path}")
    String storePathUrl;
    StoreFileRepository fileRepository;
    StoreDirRepository dirRepository;
    UserRepository userRepository;
    StorePermissionService permissionService;

    @Transactional
    public StoreFile store(MultipartFile file, StoreFile storeFile) {
        try {
            Files.copy(file.getInputStream(), getTarget(storeFile), StandardCopyOption.REPLACE_EXISTING);
            return storeFile;
        } catch (IOException ex) {
            throw new StorageException("Could not store file " + storeFile.getName(), ex);
        }
    }

    @Transactional
    public StoreFile store(StoreFile storeFile) {
        try {
            Files.createFile(getTarget(storeFile));
            return storeFile;
        } catch (IOException ex) {
            throw new StorageException("Could not store file " + storeFile.getName(), ex);
        }
    }

    @Transactional
    public StoreFile save(MultipartFile file, String username) {
        return save(file, username, null);
    }

    @Transactional
    public StoreFile save(MultipartFile file, String username, UUID dirId) {
        return save(file.getOriginalFilename(), username, file.getSize(), dirId);
    }

    @Transactional
    public StoreFile save(String name, String username) {
        return save(name, username, 0, null);
    }

    @Transactional
    public StoreFile save(String name, String username, UUID dirId) {
        return save(name, username, 0, dirId);
    }

    @Transactional
    public StoreFile save(String name, String username, long size, UUID dirId) {
        var storeFile = new StoreFile();
        storeFile.setName(name);
        storeFile.setSize(size);
        User creator = userRepository.getByUsername(username);
        storeFile.setCreator(creator);
        if (dirId != null) {
            var dir = dirRepository.findById(dirId);
            if (dir.isPresent()) storeFile.setDir(dir.get());
            else throw new StoreDirNotFoundException("Dir with id %s not found".formatted(dirId));
        }
        storeFile = fileRepository.save(storeFile);
        permissionService.addPermission(storeFile, creator, StorePermissionType.CREATOR);
        return storeFile;
    }

    private Path getTarget(StoreFile storeFile) throws IOException {
        var storePath = Path.of(storePathUrl);
        Files.createDirectories(storePath);
        return storePath.resolve(storeFile.getId().toString() + "."
                + FilenameUtils.getExtension(storeFile.getName()));
    }

    @Autowired
    public void setFileRepository(StoreFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Autowired
    public void setDirRepository(StoreDirRepository dirRepository) {
        this.dirRepository = dirRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPermissionService(StorePermissionService permissionService) {
        this.permissionService = permissionService;
    }
}