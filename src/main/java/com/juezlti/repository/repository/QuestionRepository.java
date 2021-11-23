package com.juezlti.repository.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.juezlti.repository.models.Question;
import com.juezlti.repository.models.Test;

public interface QuestionRepository extends MongoRepository<Question, String> {

	List<Question> findByTitleIgnoreCase(String title);

	List<Question> findByTypeInIgnoreCase(List<String> type);

	List<Question> findByDifficultyInIgnoreCase(List<String> difficulty);

	List<Question> findByTitleLikeIgnoreCase(String title);

	List<Question> findByIdIn(Collection<String> id);

	List<Question> findByDifficultyInIgnoreCaseAndTypeInIgnoreCase(List<String> difficulty, List<String> type);

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Question' }", count = true)
	public Long findAllQuestions();

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Question' }")
	List<Question> findAllQuestions(Pageable pageable);

	@Query("{ keywords : ?0}")
	List<Question> findByKeywords(List<String> keywords);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5},?6 :{ $in : ?7}}")
	List<Question> findByQuestions4Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, List<String> list4, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}, ?6 :{ $gte : ?7}}")
	List<Question> findByQuestions4Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, String list4, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5}}")
	List<Question> findByQuestions3Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}}")
	List<Question> findByQuestions3Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, String list3, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}}")
	List<Question> findByQuestions2Values(String parameter, List<String> list, String parameter2, List<String> list2,
			Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $gte : ?3}}")
	List<Question> findByQuestions2Values(String parameter, List<String> list, String parameter2, String list2,
			Pageable pageable);

	@Query(value = "{?0: {$in : ?1}}")
	List<Question> QueryFindByValue(String parameter, List<String> list, Pageable pageable);

	@Query(value = "{?0: {$gte : ?1}}")
	List<Question> QueryFindByValue(String parameter, String list, Pageable pageable);

	// counts

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5},?6 :{ $in : ?7}}", count = true)
	public Long findByQuestions4ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, List<String> list4);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}, ?6 :{ $gte : ?7}}", count = true)
	public Long findByQuestions4ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, String list4);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5}}", count = true)
	public Long findByQuestions3ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}}", count = true)
	public Long findByQuestions3ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, String list3);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}}", count = true)
	public Long findByQuestions2ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $gte : ?3}}", count = true)
	public Long findByQuestions2ValuesCount(String parameter, List<String> list, String parameter2, String list2);

	@Query(value = "{?0: {$in : ?1}}", count = true)
	public Long QueryFindByValueCount(String parameter, List<String> list);

	@Query(value = "{?0: {$gte : ?1}}", count = true)
	public Long QueryFindByValueCount(String parameter, String list);

	List<Test> findByKeywordsIn(List<String> keywords);

}
