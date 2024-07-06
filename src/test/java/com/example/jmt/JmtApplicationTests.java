package com.example.jmt;

import com.example.jmt.desert.model.Desert;
import com.example.jmt.desert.repository.DesertRepository;
import com.example.jmt.model.Meal;
import com.example.jmt.repository.MealRepository;
import com.example.jmt.service.MealService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class JmtApplicationTests {

	@Autowired
	MealRepository mealRepository;

	@Autowired
	MealService mealService;

	@Test
	void test() {
		// 100개의 질문 생성
		for (int i = 1; i <= 100; i++) {
			Meal meal = new Meal();
			meal.setTitle("테스트 제목 " + i);
			meal.setContent("테스트 내용 " + i);
			meal.setCreatedAt(LocalDateTime.now());
			mealRepository.save(meal);
		}

		System.out.println("완료");
	}
}
