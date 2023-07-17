package com.flowiee.app.khotailieu.service.impl;

import com.flowiee.app.common.exception.BadRequestException;
import com.flowiee.app.khotailieu.entity.DocData;
import com.flowiee.app.khotailieu.entity.Document;
import com.flowiee.app.khotailieu.model.DocMetaResponse;
import com.flowiee.app.khotailieu.repository.DocumentRepository;
import com.flowiee.app.khotailieu.service.DocDataService;
import com.flowiee.app.khotailieu.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocDataService docDataService;
    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Document> findRootDocument() {
        List<Document> listDocument = documentRepository.findRootDocument();
        return listDocument;
    }

    @Override
    public List<Document> findListDocument(int parentId) {
        return documentRepository.findListDocument(parentId);
    }

    @Override
    public Document findById(int id) {
        return documentRepository.findById(id).orElse(null);
    }

    @Override
    public Document save(Document document) {
        return documentRepository.save(document);
    }

    @Override
    public String update(Document data, int documentId) {
        Document document = this.findById(documentId);
        if (document != null) {
            document.setTen(data.getTen());
            document.setMoTa(data.getMoTa());
            documentRepository.save(document);
            return "OK";
        }
        return "NOK";
    }

    @Override
    public String updateMetadata(Integer[] docDataIds, String[] docDataValues, int documentId) {
        Document document = this.findById(documentId);
        if (document == null) {
            throw new BadRequestException();
        }
        for (int i = 0; i < docDataIds.length; i++) {
            DocData docData = docDataService.findById(docDataIds[i]);
            if (docData != null) {
                docData.setNoiDung(docDataValues[i]);
                docDataService.save(docData);
            }
        }
        return "OK";
    }

    @Override
    public String delete(int id) {
        documentRepository.deleteById(id);
        if (findById(id) == null) {
            return "OK";
        } else {
            return "NOK";
        }
    }

    @Override
    public List<DocMetaResponse> getMetadata(int documentId) {
        List<DocMetaResponse> listReturn = new ArrayList<>();

        Query result = entityManager.createQuery("SELECT d.id, d.noiDung, f.tenField, f.loaiField, f.batBuocNhap " +
                                                        "FROM DocField f " +
                                                        "LEFT JOIN DocData d ON f.id = d.docField.id " +
                                                        "WHERE d.document.id = " + documentId);
        List<Object[]> listData = result.getResultList();

        if (!listData.isEmpty()) {
            for (Object[] data : listData) {
                DocMetaResponse metadata = new DocMetaResponse();
                metadata.setDocDataId(Integer.parseInt(String.valueOf(data[0])));
                metadata.setDocDataValue(data[1] != null ? String.valueOf(data[1]) : "");
                metadata.setDocFieldName(String.valueOf(data[2]));
                metadata.setDocFieldTypeInput(String.valueOf(data[3]));
                metadata.setDocFieldRequired(String.valueOf(data[4]).equals("1") ? true : false);
                listReturn.add(metadata);
            }
        }

        return listReturn;
    }
}