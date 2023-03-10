package com.mainproject.back.tag.service;

import com.mainproject.back.tag.entity.Tag;
import com.mainproject.back.tag.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

  private final TagRepository tagRepository;

  public List<Tag> findAllTags() {
    return tagRepository.findAll();
  }
}
