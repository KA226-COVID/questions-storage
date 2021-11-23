package com.juezlti.repository.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.juezlti.repository.models.Question;
import com.juezlti.repository.models.Test;

public interface TestRepository extends MongoRepository<Test, String> {

	@org.springframework.data.mongodb.repository.Query(value = "{ 'questions': { $elemMatch: { 'id' : ?0 } }}")
	Test findByQuestionId(String id);

	Test findByIdOrQuestionsId(String value, String value1);

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Test', 'id' : ?0 }}")
	Test findTestById(String id);

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Test' }")
	List<Test> findAllTest(Pageable pageable);

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Test' }")
	List<Test> findAllTest();

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Question', 'id' : ?0 }")
	Question findQuestionById(String id);

	List<Test> findByQuestionsTypeIn(List<String> value);

	List<Test> findByQuestionsDifficultyIn(List<String> value);

	List<Test> findByQuestionsDifficultyInAndQuestionsTypeInAndQuestionsKeywordsIn(List<String> difficulty,
			List<String> type, List<String> keywords);


	// Buenos

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5},?6 :{ $in : ?7}}}}")
	List<Test> findByQuestions4Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, List<String> list4, Pageable pageable);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}, ?6 :{ $gte : ?7}}}}")
	List<Test> findByQuestions4Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, String list4, Pageable pageable);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5}}}}")
	List<Test> findByQuestions3Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, Pageable pageable);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}}}}")
	List<Test> findByQuestions3Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, String list3, Pageable pageable);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}}}}")
	List<Test> findByQuestions2Values(String parameter, List<String> list, String parameter2, List<String> list2,
			Pageable pageable);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $gte : ?3}}}}")
	List<Test> findByQuestions2Values(String parameter, List<String> list, String parameter2, String list2,
			Pageable pageable);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}}}}")
	List<Test> QueryFindByValue(String parameter, List<String> list, Pageable pageable);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$gte : ?1}}}}")
	List<Test> QueryFindByValue(String parameter, String list, Pageable pageable);

	// counts

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5},?6 :{ $in : ?7}}}}", count = true)
	public Long findByQuestions4ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, List<String> list4);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}, ?6 :{ $gte : ?7}}}}", count = true)
	public Long findByQuestions4ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, String list4);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5}}}}", count = true)
	public Long findByQuestions3ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}}}}", count = true)
	public Long findByQuestions3ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, String list3);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $in : ?3}}}}", count = true)
	public Long findByQuestions2ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}, ?2 :{ $gte : ?3}}}}", count = true)
	public Long findByQuestions2ValuesCount(String parameter, List<String> list, String parameter2, String list2);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$in : ?1}}}}", count = true)
	public Long QueryFindByValueCount(String parameter, List<String> list);

	@Query(value = "{'questions':{'$elemMatch':{?0: {$gte : ?1}}}}", count = true)
	public Long QueryFindByValueCount(String parameter, String list);

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Test' }", count = true)
	public Long findAllTestCount();

	List<Test> findByQuestionsKeywordsIn(List<String> keywords);

}
