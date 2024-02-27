package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.models.Answer;
import com.lucasdavi.quizz.services.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping
    public ResponseEntity<Answer> createAnswer(@RequestBody Answer answer) {
        return ResponseEntity.ok(this.answerService.saveAnswer(answer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Answer> getAnswerById(@PathVariable Long id) {
        return this.answerService.getAnswerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @GetMapping("/question/{questionId}")
//    public ResponseEntity<List<Answer>> getAnswersByQuestionId(@PathVariable Long questionId) {
//        return ResponseEntity.ok(answerService.getAnswerById(questionId));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<Answer> updateAnswer(@PathVariable Long id, @RequestBody Answer answer) {
        return ResponseEntity.ok(this.answerService.updateAnswerById(id, answer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        this.answerService.deleteAnswerById(id);
        return ResponseEntity.ok().build();
    }
}
