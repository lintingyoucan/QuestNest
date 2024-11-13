package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface DraftService {
    Map<String,Object> saveDraft(String email, String title, String content);

    Map<String,String> updateDraft(Integer draftId, String content);

    Map<String, Object> postDraft(Integer draftId);

    Map<String, String> deleteDraft(Integer draftId);

    Map<String,Object> showDraftByDraftId(Integer draftId);

    Map<String,Object> showDraftByUserId(String email);

}
