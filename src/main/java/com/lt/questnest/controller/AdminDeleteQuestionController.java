package com.lt.questnest.controller;

import com.lt.questnest.service.AdminCheckQuestionService;
import com.lt.questnest.service.AdminDeleteQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminDeleteQuestionController {

    @Autowired
    AdminDeleteQuestionService adminDeleteQuestionService;

    @Autowired
    AdminCheckQuestionService adminCheckQuestionService;

    /**
     * 20241024
     * 审核不通过问题
     * @param questionId
     * @param reason
     * @param session
     * @return
     */
    @PostMapping("/failToQuestion")
    public ResponseEntity<Map<String, Object>> failToQuestion(@RequestParam("questionId") Integer questionId,
                                                             @RequestParam("reason") String reason,
                                                             HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String account = (String)session.getAttribute("account");
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,String> map = adminDeleteQuestionService.failToQuestion(account,questionId,reason);

                if (map.containsValue("success")){
                    result.put("status","success");
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status","error");
                    result.put("message",map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }
            } else {
                result.put("status", "error");
                result.put("message", "管理员未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "问题审核不通过失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示未审核问题
     * 20241106
     * @param session
     * @return
     */
    @GetMapping("/showUncheckQuestion")
    public ResponseEntity<Map<String, Object>> showUncheckQuestion(HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String account = (String)session.getAttribute("account");
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,Object> map = adminCheckQuestionService.getQuestion();

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("question",map.get("question")); // 返回问题列表
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status","error");
                    result.put("message",map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }
            } else {
                result.put("status", "error");
                result.put("message", "管理员未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "查看未审核问题失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }
}
