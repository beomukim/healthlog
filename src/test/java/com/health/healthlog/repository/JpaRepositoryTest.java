package com.health.healthlog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.health.healthlog.domain.Article;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@DisplayName("JPA 연결 테스트")
@SpringBootTest
public class JpaRepositoryTest {

    private ArticleRepository articleRepository;
    private TrainingRepository trainingRepository;

    public JpaRepositoryTest(
            @Autowired ArticleRepository articleRepository,
            @Autowired TrainingRepository trainingRepository) {
        this.articleRepository = articleRepository;
        this.trainingRepository = trainingRepository;
    }

    @DisplayName("select 테스트")
    @Test
    void whenSelecting_thenWorksFine() {
        //when
        List<Article> articles = articleRepository.findAll();

        //then
        assertThat(articles).isNotNull().hasSize(2);
    }

    @DisplayName("insert 테스트")
    @Test
    void givenTestData_whenInserting_thenWorksFine() {
        // Given
        long previousCount = articleRepository.count();
        Article article = new Article("new content");

        // When
        articleRepository.save(article);

        // Then
        assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("update 테스트")
    @Test
    void givenTestData_whenUpdating_thenWorksFine() {
        // Given
        Article article = articleRepository.findById(1L).orElseThrow();
        String updatedContent = "this is health log.";
        article.setContent(updatedContent);

        // When
        Article savedArticle = articleRepository.saveAndFlush(article);

        // Then
        assertThat(savedArticle).hasFieldOrPropertyWithValue("content", updatedContent);
    }

    @DisplayName("delete 테스트")
    @Test
    void givenTestData_whenDeleting_thenWorksFine() {
        // Given
        Article article = articleRepository.findById(13L).orElseThrow();

        long previousArticleCount = articleRepository.count();
        long previousTrainingCommentCount = trainingRepository.count();
        int deletedCommentsSize = article.getTrainings().size();
        //TODO: LazyInitializationException 발생

        // When
        articleRepository.delete(article);

        // Then
        assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
        assertThat(trainingRepository.count()).isEqualTo(previousTrainingCommentCount - deletedCommentsSize);
    }
}
