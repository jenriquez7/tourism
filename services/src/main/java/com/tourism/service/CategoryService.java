package com.tourism.service;

import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Category;
import io.vavr.control.Either;

public interface CategoryService {

    Either<ErrorDto[], Category> create(Category category);

    Either<ErrorDto[], Category> update(Category category);

    Either<ErrorDto[], Category> delete(Integer id);

    Either<ErrorDto[], Category> getById(Integer id);

    Either<ErrorDto[], Category[]> findByName(String name);

    Either<ErrorDto[], Category[]> findAll();
}
