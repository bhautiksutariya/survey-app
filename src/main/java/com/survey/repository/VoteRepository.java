package com.survey.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.survey.model.ChoiceVoteCount;
import com.survey.model.Vote;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
	
    @Query("SELECT NEW com.survey.model.ChoiceVoteCount(v.choice.id, count(v.id)) FROM Vote v WHERE v.survey.id in :surveyIds GROUP BY v.choice.id")
    List<ChoiceVoteCount> countBySurveyIdInGroupByChoiceId(@Param("surveyIds") List<Long> surveyIds);

    @Query("SELECT NEW com.survey.model.ChoiceVoteCount(v.choice.id, count(v.id)) FROM Vote v WHERE v.survey.id = :surveyId GROUP BY v.choice.id")
    List<ChoiceVoteCount> countBySurveyIdGroupByChoiceId(@Param("surveyId") Long surveyId);

    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.survey.id in :surveyIds")
    List<Vote> findByUserIdAndSurveyIdIn(@Param("userId") Long userId, @Param("surveyIds") List<Long> surveyIds);

    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.survey.id = :surveyId")
    Vote findByUserIdAndSurveyId(@Param("userId") Long userId, @Param("surveyId") Long surveyId);

    @Query("SELECT COUNT(v.id) from Vote v where v.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT v.survey.id FROM Vote v WHERE v.user.id = :userId")
    Page<Long> findVotedSurveyIdsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    long deleteByChoice_IdIn(List<Long> ids);
}
