package com.lt.questnest.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.*;
import com.lt.questnest.mapper.*;
import com.lt.questnest.service.FileService;
import com.lt.questnest.service.QuestionService;
import com.sun.corba.se.impl.ior.OldJIDLObjectKeyTemplate;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    TopicMapper topicMapper;

    @Autowired
    QuestionTopicMapper questionTopicMapper;

    @Autowired
    FileService fileService;

    @Autowired
    UserQuestionArticleViewMapper userQuestionArticleViewMapper;

    @Autowired
    ArticleMapper articleMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    // 添加问题:注意事务处理，数据库的更新要统一
    @Transactional
    public Map<String,Object> addQuestion(String email,String title, String content, Set<String> topics){

        Map<String,Object> result = new HashMap<>();

        // 对传入参数判空
        if (title == null || title.isEmpty()){
            result.put("status","error");
            result.put("msg","title 不能为空");
            return result;
        }
        if (content == null || content.isEmpty()){
            result.put("status","error");
            result.put("msg","content 不能为空");
            return result;
        }
        if (topics == null || topics.isEmpty()){
            result.put("status","error");
            result.put("msg","topic 不能为空");
            return result;
        }

        // 获取提问用户
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())) { // 如果用户不存在或者已注销
            result.put("status","error");
            result.put("msg","用户不存在或已注销");
            return result;
        }
        // 用户存在，获取userId
        Integer userId = user.getId();

        // 判断title是否已存在
        Question question = questionMapper.findByTitle(title);

        if (question != null){ // title存在
            result.put("status","error");
            result.put("msg","title 已存在");
            return result;
        }

        // title不存在，保存到数据库
        question = new Question(userId,title,content);
        // 处理数据库信息添加结果
        Integer addQuestion = questionMapper.addQuestion(question);
        if (addQuestion == null || addQuestion > 0) {
            logger.info("question添加成功!");
            // 返回添加后的问题ID
            result.put("questionId",question.getQuestionId());
        } else {
            logger.info("question添加失败!");
            result.put("status","error");
            result.put("msg","question在数据库添加失败");
            return result;
        }
        // 获取questionId
        Integer questionId = question.getQuestionId();
        logger.info("获取添加问题后的问题id:{}",questionId);

        for (String topicName : topics) {

            // 如果topic存在则退出循环，如果不存在添加进数据库
            Topic topic = topicMapper.findByTopic(topicName);
            if (topic == null){
                // topic放进数据库
                Integer addTopic = topicMapper.addTopic(topicName);
                if (addTopic == null || addTopic <= 0){
                    logger.info("topic添加失败!");
                    result.put("status","error");
                    result.put("msg","topic在数据库添加失败");
                    return result;
                }
                topic = topicMapper.findByTopic(topicName);// 重新获取添加后的 topic 信息
            }

            // 将question-topic放进数据库
            QuestionTopic questionTopic = new QuestionTopic();
            questionTopic.setQuestionId(questionId);
            questionTopic.setTopicId(topic.getTopicId());

            Integer addQuestionTopic = questionTopicMapper.add(questionTopic);
            if (addQuestionTopic == null || addQuestionTopic <= 0){
                logger.info("QuestionTopic添加失败!");
                result.put("status","error");
                result.put("msg","question_topic在数据库添加失败");
                return result;
            }

        }

        result.put("status","success");
        result.put("msg","问题添加相关数据库处理成功!");

        return result;
    }

    // 修改问题
    @Transactional
    public Map<String,String> updateQuestion(Integer questionId,String email,String title,String content,Set<String> topics){

        Map<String,String> result = new HashMap<>();
        // 参数判空
        if (questionId == null || questionId <= 0){
            result.put("status","error");
            result.put("msg","questionId为空或无效");
            return result;
        }
        if (email == null || email.isEmpty()){
            result.put("status","error");
            result.put("msg","email为空");
            return result;
        }
        if (topics == null || topics.isEmpty()){
            result.put("status","error");
            result.put("msg","topic 不能为空");
            return result;
        }

        // questionId是否存在
        Question question = questionMapper.findByQuestionId(questionId);
        if (question == null){
            result.put("status","error");
            result.put("msg","问题不存在");
            return result;
        }

        // question的作者和登录者是否同一个
        Integer authorId = question.getUserId();
        Integer userId = userMapper.getUserByEmail(email).getId();
        if (authorId != userId){
            result.put("status","error");
            result.put("msg","非法操作");
            return result;
        }

        // 检查修改的title和原来的title是否一致
        String preTitle = question.getTitle();
        if (preTitle.equals(title)){ // 如果一致

            // 修改question的content
            Integer updateQuestionResult = questionMapper.updateQuestionContent(questionId,content);
            if (updateQuestionResult == null || updateQuestionResult <= 0){
                result.put("status","error");
                result.put("msg","更新问题内容失败");
                return result;
            }

        } else {

            // 如果不一致，检查修改后的title在数据库中是否存在
            Question questionByTitle = questionMapper.findByTitle(title);
            if (questionByTitle != null){
                result.put("status","error");
                result.put("msg","标题已存在");
                return result;
            }

            // 保存数据到question
            question = new Question();
            question.setQuestionId(questionId);
            question.setTitle(title);
            question.setContent(content);
            Integer updateQuestionResult = questionMapper.updateQuestion(question);
            if (updateQuestionResult == null || updateQuestionResult <= 0){
                throw new RuntimeException("更新问题内容失败");
            }

        }

        // 遍历topic
        for (String topicName : topics) {

            // 如果topic存在则退出循环，如果不存在添加进数据库
            Topic topic = topicMapper.findByTopic(topicName);
            if (topic == null) {
                // topic放进数据库
                Integer addTopic = topicMapper.addTopic(topicName);
                if (addTopic == null || addTopic <= 0) {
                    logger.info("topic添加失败!");
                    throw new RuntimeException("topic在数据库添加失败");
                }
                topic = topicMapper.findByTopic(topicName);// 重新获取添加后的 topic 信息
            }

            // 将question-topic放进数据库
            QuestionTopic questionTopic = new QuestionTopic();
            questionTopic.setQuestionId(questionId);
            questionTopic.setTopicId(topic.getTopicId());

            QuestionTopic questionTopicTemp = questionTopicMapper.findByBoth(questionTopic);// 从数据库找是否存在关系
            if (questionTopicTemp == null){ // 如果不存在
                Integer addQuestionTopic = questionTopicMapper.add(questionTopic);
                if (addQuestionTopic == null || addQuestionTopic <= 0){
                    logger.info("QuestionTopic添加失败!");
                    throw new RuntimeException("question_topic在数据库添加失败");
                }
            }
        }

        // 检查question-topic,找出question-topic关系，如果之前存在的话题，现在不存在，那么修改state=0
        List<Integer> topicIds = questionTopicMapper.findTopicByQuestionId(questionId);

        // 如果questionTopics中每一条在topics中都没有找到相同的，那么设置state=0
        for (Integer topicId : topicIds) {

            // 找到topic的name
            String topicName = topicMapper.findByTopicId(topicId);
            if (!topics.contains(topicName)) { // 如果找不到相同的
                QuestionTopic questionTopic = new QuestionTopic();
                questionTopic.setTopicId(topicId);
                questionTopic.setQuestionId(questionId);

                // 更新state
                Integer deleteResult = questionTopicMapper.delete(questionTopic);
                if (deleteResult == null || deleteResult <= 0) {
                    throw new RuntimeException("删除话题标签失败");
                }
            }
        }

        result.put("status","success");
        return result;
    }


    private JiebaSegmenter segmenter = new JiebaSegmenter(); // 初始化Jieba分词器

    /**
     * 搜索方法，根据给定的关键词搜索问题，并返回符合相似度阈值的结果
     * @param keyword
     * @return
     */
    public Map<String, Object> search(String keyword) {

        logger.info("进入search方法");
        // 从数据库中获取所有的问题列表（使用QuestionMapper调用持久层）
        List<Question> allQuestions = questionMapper.findAllQuestion();

        // 存储匹配到的符合条件的问题和相似度
        List<Question> matchedQuestions = new ArrayList<>();
        List<Double> similarities = new ArrayList<>();
        double threshold = 0.0; // 设置Jaccard相似度的阈值，低于这个值的结果将不被返回

        logger.info("对问题进行遍历");
        // 遍历所有的问题，计算每个问题的标题与输入关键词的Jaccard相似度
        for (Question question : allQuestions) {
            // 计算关键词和问题标题之间的相似度
            double similarity = calculateJaccardSimilarity(keyword, question.getTitle());
            logger.info("相似度为:{}",similarity);

            // 如果相似度大于阈值，保存该问题以及对应的相似度
            if (similarity > threshold) {
                matchedQuestions.add(question);
                logger.info("question列表有:{}",matchedQuestions);
                similarities.add(similarity);
            }
        }

        // 创建一个Map来存储返回结果
        Map<String, Object> result = new HashMap<>();
        // 如果没有找到问题
        if (matchedQuestions.isEmpty()) {
            result.put("msg", "没有找到问题");
            result.put("similarities", similarities);
            return result;
        }

        // 创建一个数据结果列表
        List<Map<String, Object>> questionList = new ArrayList<>();
        for (Question question : matchedQuestions) {
            Map<String,Object> questionItem = new HashMap<>();
            questionItem.put("questionId",question.getQuestionId());
            questionItem.put("title",question.getTitle());

            questionList.add(questionItem);
        }

        result.put("questions", questionList); // 将匹配到的问题列表放入Map
        result.put("similarities", similarities);  // 将对应的问题相似度列表放入Map
        logger.info("处理完后的匹配question列表:{}",questionList);

        // 返回包含问题列表和相似度的Map
        return result;
    }


    /**
     * 计算Jaccard相似度的方法，使用jieba分词器对输入关键词和标题分词并进行相似度计算
     * @param keyword
     * @param title
     * @return
     */
    private double calculateJaccardSimilarity(String keyword, String title) {
        logger.info("进入计算Jaccard相似度的方法");

        // 使用jieba对关键词和问题标题进行分词
        Set<String> keywordSet = new HashSet<>(segmentWords(keyword));
        Set<String> titleSet = new HashSet<>(segmentWords(title));

        // 计算关键词和标题的交集
        Set<String> intersection = new HashSet<>(keywordSet);
        intersection.retainAll(titleSet); // 保留交集部分
        logger.info("交集为: {}", intersection);

        // 计算关键词和标题的并集
        Set<String> union = new HashSet<>(keywordSet);
        union.addAll(titleSet); // 将两个集合的所有词组合起来形成并集
        logger.info("并集为: {}", union);

        // 如果并集为空，返回相似度为0（避免除以0的错误）
        if (union.size() == 0) {
            return 0.0;
        }

        // 计算Jaccard相似度，公式：交集大小 / 并集大小
        return (double) intersection.size() / union.size();
    }

    /**
     * jieba分词器的辅助方法，将输入字符串分词并返回分词结果
     * @param text
     * @return
     */
    public List<String> segmentWords(String text) {
        // 使用精确模式进行分词
        List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.INDEX);

        // 提取分词结果中的词语
        List<String> words = new ArrayList<>();
        for (SegToken token : tokens) {
            words.add(token.word);
        }

        logger.info("精确模式分词结果: {}", words);
        return words;
    }


    @Value("${moonshot.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 从AI获取回答
    public Map<String,Object> searchByAI(String keyword){

        Map<String,Object> result = new HashMap<>();

        // 参数判空
        if (keyword == null || keyword.isEmpty()){
            result.put("status","error");
            result.put("msg","输入内容为空");
            return result;
        }
        String url = "https://api.moonshot.cn/v1/chat/completions";

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 设置请求体
        String requestBody = String.format(
                "{\"model\":\"moonshot-v1-8k\",\"messages\":[{\"role\":\"system\",\"content\":\"你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一切涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。\"},{\"role\":\"user\",\"content\":\"%s\"}]}",
                keyword
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        // 发送请求并获得响应
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        String content = "";
        try {
            // 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();

            // 解析 JSON 字符串
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            //JsonNode rootNode = objectMapper.readTree("{\"id\":\"chatcmpl-671b549fb358d403bbc8e815\",\"object\":\"chat.completion\",\"created\":1729844386,\"model\":\"moonshot-v1-8k\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"好的，来听一个有趣的小故事：\\n\\n有一天，一位顾客走进了一家餐厅，对服务员说：“我要一份牛排，但请确保它非常非常熟，我可不想吃生的。”\\n\\n服务员点头表示理解，然后走进了厨房。过了一会儿，服务员端着一个盘子回来了，盘子上放着一块烧焦的牛排，几乎成了炭。\\n\\n顾客看着这块牛排，惊讶地说：“这牛排也太熟了吧，它看起来像是从火山里拿出来的！”\\n\\n服务员微笑着回答：“先生，您说您想要非常非常熟的牛排，所以我们厨师决定用火山烤它，这样它就熟透了。不过，我们厨师也说，下次如果您想要稍微生一点的牛排，只要说‘五分熟’就可以了。”\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":81,\"completion_tokens\":155,\"total_tokens\":236}}");
            logger.info("获取的rootNode:{}",rootNode);
            JsonNode choicesNode = rootNode.get("choices");
            logger.info("获取的choicesNode:{}",choicesNode);
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                // 获取第一个 choice 中的 message 的 content
                JsonNode contentNode = choicesNode.get(0).get("message").get("content");
                logger.info("获取的contentNode:{}",contentNode);
                content = contentNode.asText();
                logger.info("获取的内容:{}",content);
                result.put("content",content);
            }

        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","解析JSON失败");
            e.printStackTrace();
            return result;
        }

        result.put("status","success");
        return result;
    }

    // 返回违规问题
    public Map<String,Object> getIllegalQuestion(String email){
        Map<String,Object> result = new HashMap<>();

        // 找出用户ID
        User user = userMapper.getUserByEmail(email);
        Integer userId  = user.getId();

        // 找出用户的问题状态state = 0
        List<Question> questions = questionMapper.findIllegalQuestion(userId);
        if (questions == null || questions.isEmpty()){ // 如果没有违规问题，那么返回空列表
            result.put("status","success");
            result.put("illegalQuestion",new ArrayList<>());
            return result;
        }

        // 数据列表
        List<Map<String, Object>> questionList = new ArrayList<>();
        // 返回问题
        for (Question question : questions) {
            Map<String,Object> questionItem = new HashMap<>();
            questionItem.put("title",question.getTitle());
            questionItem.put("content",question.getContent());
            questionItem.put("questionId",question.getQuestionId());

            questionList.add(questionItem);
        }

        result.put("illegalQuestion",questionList);
        result.put("status","success");
        return result;
    }

    // 显示问题回答数目
    public Integer getAnswerNumber(String title) {
        Map<String, Object> result = new HashMap<>();

        // 根据title找questionId
        Question question = questionMapper.findByTitle(title);
        Integer questionId = question.getQuestionId();
        // 根据questionId找出文章回答数目
        Integer answerNumber = articleMapper.getQuestionAnswerNumber(questionId);
        return answerNumber;
    }

    // 显示问题对应的回答
    public Map<String,Object> getQuestionAndArticle(Integer questionId){
        Map<String,Object> result = new HashMap<>();

        // 获取
        List<UserQuestionArticleView> userQuestionArticles = userQuestionArticleViewMapper.get(questionId);

        List<Map<String,Object>> userQuestionArticleList = new ArrayList<>();
        for (UserQuestionArticleView userQuestionArticle : userQuestionArticles) {

            int count = 1;
            // 取出问题相关信息
            if (count == 1){
                result.put("questionId",questionId);
                result.put("questionTitle",userQuestionArticle.getQuestionTitle());
                result.put("questionContent",userQuestionArticle.getQuestionContent());
                count = 0;
            }

            // 如果返回文章内容为空，说明该问题暂时没有人回答
            if ((userQuestionArticle.getArticleContent()) == null || (userQuestionArticle.getArticleContent()).isEmpty()){
                result.put("status","success");
                return result;
            }

            // 取出文章相关信息
            Map<String,Object> userQuestionArticleItem = new HashMap<>();
            userQuestionArticleItem.put("articleId",userQuestionArticle.getArticleId());
            userQuestionArticleItem.put("articleContent",userQuestionArticle.getArticleContent());
            userQuestionArticleItem.put("articleUsername",userQuestionArticle.getArticleUsername());
            String articleEmail = userQuestionArticle.getArticleEmail();

            userQuestionArticleItem.put("articleHeadUrl","http://192.168.178.78:8080/images?email="+articleEmail);

            userQuestionArticleList.add(userQuestionArticleItem);
        }

        result.put("userQuestionArticle",userQuestionArticleList);
        result.put("status","success");
        return result;
    }
}
