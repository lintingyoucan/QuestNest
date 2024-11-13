package com.lt.questnest.controller;

import com.lt.questnest.service.AdminDeleteCommentService;
import com.lt.questnest.service.ReportCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminCommentController {

    @Autowired
    AdminDeleteCommentService adminDeleteCommentService;

    @Autowired
    ReportCommentService reportCommentService;

    private static final Logger logger = LoggerFactory.getLogger(AdminCommentController.class);


    /**
     * 审核不通过评论（直接删除）
     * 20241024
     * @param commentId
     * @param reason
     * @return
     */
    @PostMapping("/deleteComment")
    public ResponseEntity<Map<String, Object>> deleteComment(@RequestParam("commentId") Integer commentId,
                                                             @RequestParam("reason") String reason,
                                                             Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String account = principal.getName();
        logger.info("取出account:{}",account);
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,String> map = adminDeleteCommentService.deleteComment(account,commentId,reason);

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
            result.put("message", "评论审核不通过失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示举报评论信息
     * 20241107
     * @return
     */
    @GetMapping("/showReportComment")
    public ResponseEntity<Map<String, Object>> showReportComment(Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String account = principal.getName();
        logger.info("取出account:{}",account);
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,Object> map = reportCommentService.showReportComment();

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("reportComment",map.get("reportComment")); // 返回数据列表
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
            result.put("message", "显示举报评论信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 处理举报信息后，删除举报信息
     * 20241107
     * @param reportCommentId
     * @return
     */
    @PostMapping("/readReportComment")
    public ResponseEntity<Map<String, Object>> readReportComment(@RequestParam("reportCommentId") Integer reportCommentId,
                                                                 Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String account = principal.getName();
        logger.info("取出account:{}",account);
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,Object> map = reportCommentService.deleteReportComment(reportCommentId);

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
            result.put("message", "删除举报信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

}
