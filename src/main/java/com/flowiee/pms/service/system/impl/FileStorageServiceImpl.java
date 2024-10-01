package com.flowiee.pms.service.system.impl;

import com.flowiee.pms.config.StartUp;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.entity.system.SystemConfig;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.repository.system.ConfigRepository;
import com.flowiee.pms.repository.system.FileStorageRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.system.FileStorageService;

import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.FileUtils;
import com.flowiee.pms.utils.constants.ConfigCode;
import com.flowiee.pms.utils.constants.ErrorCode;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FileStorageServiceImpl extends BaseService implements FileStorageService {
    FileStorageRepository fileRepository;
    ConfigRepository configRepository;

    @Override
    public List<FileStorage> findAll() {
        return fileRepository.findAll();
    }

    @Override
    public Optional<FileStorage> findById(Integer fileId) {
        return fileRepository.findById(fileId);
    }

    @Transactional
    @Override
    public FileStorage save(FileStorage fileStorage) {
        FileStorage fileStorageSaved = fileRepository.save(fileStorage);

        vldResourceUploadPath(true);
        Path pathDest = Paths.get(CommonUtils.getPathDirectory(MODULE.PRODUCT) + File.separator + fileStorageSaved.getStorageName());
        try {
            saveFileAttach(fileStorage.getFileAttach(), pathDest);
        } catch (IOException ex) {
            throw new AppException("An error occurred while saving the attachment!", ex);
        }

        return fileStorageSaved;
    }

    @Override
    public FileStorage update(FileStorage entity, Integer entityId) {
        return null;
    }

    @Override
    public String saveFileOfImport(MultipartFile fileImport, FileStorage fileInfo) throws IOException {
        fileRepository.save(fileInfo);
        fileInfo.setStorageName("I_" + fileInfo.getStorageName());
        fileImport.transferTo(Paths.get(CommonUtils.getPathDirectory(fileInfo.getModule()) + "/" + fileInfo.getStorageName()));
        return "OK";
    }

    @Override
    public void saveFileAttach(MultipartFile multipartFile, Path dest) throws IOException {
        if (vldResourceUploadPath(true)) {
            multipartFile.transferTo(dest);
        }
    }

    @Override
    public String delete(Integer fileId) {
        Optional<FileStorage> fileStorage = fileRepository.findById(fileId);
        if (fileStorage.isEmpty()) {
            throw new BadRequestException("File not found!");
        }
        fileRepository.deleteById(fileId);
        File file = new File(StartUp.getResourceUploadPath() + "/" + fileStorage.get().getDirectoryPath() + "/" + fileStorage.get().getStorageName());
        if (file.exists() && file.delete()) {
            return MessageCode.DELETE_SUCCESS.getDescription();
        }
        return String.format(ErrorCode.DELETE_ERROR_OCCURRED.getDescription(), "file");
    }

    private boolean vldResourceUploadPath(boolean throwException) {
        if (StartUp.getResourceUploadPath() == null) {
            SystemConfig resourceUploadPathConfig = configRepository.findByCode(ConfigCode.resourceUploadPath.name());
            if (resourceUploadPathConfig != null && ObjectUtils.isNotEmpty(resourceUploadPathConfig.getValue())) {
                StartUp.mvResourceUploadPath = resourceUploadPathConfig.getValue();
                return true;
            } else {
                if (throwException) {
                    throw new AppException("The uploaded file saving directory is not configured, please try again later!");
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}