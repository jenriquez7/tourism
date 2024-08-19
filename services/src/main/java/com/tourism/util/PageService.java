package com.tourism.util;

import com.tourism.dto.request.PageableRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PageService {

    public Pageable createSortedPageable(PageableRequest paging) {
        if (paging.getSort() == null || paging.getSort().length == 0) {
            return PageRequest.of(paging.getPage(), paging.getSize());
        }
        List<Sort.Order> orders = Arrays.stream(paging.getSort())
                .map(field -> new Sort.Order(paging.getSortType(), field))
                .toList();
        return PageRequest.of(paging.getPage(), paging.getSize(), Sort.by(orders));
    }
}
