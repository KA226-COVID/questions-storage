package com.juezlti.repository.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.juezlti.repository.models.Exercise;
import com.juezlti.repository.models.Test;

public interface ExerciseRepository extends MongoRepository<Exercise, String> {

	List<Exercise> findByTitleIgnoreCase(String title);

	List<Exercise> findByTypeInIgnoreCase(List<String> type);

	List<Exercise> findByDifficultyInIgnoreCase(List<String> difficulty);

	List<Exercise> findByTitleLikeIgnoreCase(String title);

	List<Exercise> findByIdIn(Collection<String> id);

	List<Exercise> findByDifficultyInIgnoreCaseAndTypeInIgnoreCase(List<String> difficulty, List<String> type);

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Exercise' }", count = true)
	public Long findAllExercises();

	@Query(value = "{ '_class' : 'com.juezlti.repository.models.Exercise' }")
	List<Exercise> findAllExercises(Pageable pageable);

	@Query("{ keywords : ?0}")
	List<Exercise> findByKeywords(List<String> keywords);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5},?6 :{ $in : ?7}}")
	List<Exercise> findByExercises4Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, List<String> list4, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}, ?6 :{ $gte : ?7}}")
	List<Exercise> findByExercises4Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, String list4, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5}}")
	List<Exercise> findByExercises3Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}}")
	List<Exercise> findByExercises3Values(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, String list3, Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}}")
	List<Exercise> findByExercises2Values(String parameter, List<String> list, String parameter2, List<String> list2,
			Pageable pageable);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $gte : ?3}}")
	List<Exercise> findByExercises2Values(String parameter, List<String> list, String parameter2, String list2,
			Pageable pageable);

	@Query(value = "{?0: {$in : ?1}}")
	List<Exercise> QueryFindByValue(String parameter, List<String> list, Pageable pageable);

	@Query(value = "{?0: {$gte : ?1}}")
	List<Exercise> QueryFindByValue(String parameter, String list, Pageable pageable);

	// counts

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5},?6 :{ $in : ?7}}", count = true)
	public Long findByExercises4ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, List<String> list4);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}, ?6 :{ $gte : ?7}}", count = true)
	public Long findByExercises4ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3, String parameter4, String list4);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $in : ?5}}", count = true)
	public Long findByExercises3ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, List<String> list3);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}, ?4 :{ $gte : ?5}}", count = true)
	public Long findByExercises3ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2,
			String parameter3, String list3);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $in : ?3}}", count = true)
	public Long findByExercises2ValuesCount(String parameter, List<String> list, String parameter2, List<String> list2);

	@Query(value = "{?0: {$in : ?1}, ?2 :{ $gte : ?3}}", count = true)
	public Long findByExercises2ValuesCount(String parameter, List<String> list, String parameter2, String list2);

	@Query(value = "{?0: {$in : ?1}}", count = true)
	public Long QueryFindByValueCount(String parameter, List<String> list);

	@Query(value = "{?0: {$gte : ?1}}", count = true)
	public Long QueryFindByValueCount(String parameter, String list);

	List<Test> findByKeywordsIn(List<String> keywords);

}
