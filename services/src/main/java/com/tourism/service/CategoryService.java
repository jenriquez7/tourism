package com.tourism.service;

import io.vavr.control.Either;

import org.springframework.data.domain.Page;

import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Category;

public interface CategoryService {

   Either<ErrorDto[], Category> create(Category category);

   Either<ErrorDto[], Category> update(Category category);

   Either<ErrorDto[], Category> delete(Integer id);

   Either<ErrorDto[], Category> getById(Integer id);

   Either<ErrorDto[], Page<Category>> findByName(String name, PageableRequest paging);

   Either<ErrorDto[], Page<Category>> findAll(PageableRequest paging);

}
