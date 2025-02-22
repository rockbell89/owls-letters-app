package com.mainproject.back.letter.mapper;

import com.mainproject.back.letter.dto.LetterListDto;
import com.mainproject.back.letter.dto.LetterPostDto;
import com.mainproject.back.letter.dto.LetterResponseDto;
import com.mainproject.back.letter.entity.Letter;
import com.mainproject.back.member.dto.MemberSimpleDto;
import com.mainproject.back.member.entity.Member;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LetterMapper {

  default Letter LetterPostDtoToLetter(LetterPostDto letterPostDto) {
    if (letterPostDto == null) {
      return null;
    } else {
      Letter.LetterBuilder letter = Letter.builder();
      letter.body(letterPostDto.getBody()).type(letterPostDto.getType());
      ArrayList<String> arrayList = letterPostDto.getPhotoUrl();
      if (arrayList != null) {
        letter.photoUrl(new ArrayList<>(arrayList));
      }
      letter.sender(Member.builder().memberId(letterPostDto.getSenderId()).build());
      letter.receiver(Member.builder().memberId(letterPostDto.getReceiverId()).build());
      return letter.build();
    }
  }

  default LetterResponseDto LetterToLetterResponseDto(Letter letter, long memberId) {
    if (letter == null) {
      return null;
    }
    LetterResponseDto.LetterResponseDtoBuilder builder = LetterResponseDto.builder()
        .letterId(letter.getLetterId())
        .type(letter.getType())
        .createdAt(letter.getCreatedAt())
        .availableAt(letter.getAvailableAt());
    if (letter.getSender() == null) {
      builder.sender(null);
    } else {
      builder.sender(letter.getSender().getName()).senderId(letter.getSender().getMemberId());
    }
    if (letter.getReceiver() == null) {
      builder.receiver(null);
    } else {
      builder.receiver(letter.getReceiver().getName());
    }
    if (letter.getAvailableAt().isBefore(LocalDateTime.now()) || letter.getSender().getMemberId() == memberId) {
      builder.body(letter.getBody()).photoUrl(letter.getPhotoUrl());
    }

    return builder.build();
  }

  default Page<LetterListDto> pageLetterToPageLetterListDtoPage(Page<Letter> letterPage,
      long memberId) {
    return letterPage.map(letter -> letterToLetterListDto(letter, memberId));
  }

  default LetterListDto letterToLetterListDto(Letter letter, long memberId) {
    Member sender = letter.getSender();
    Member receiver = letter.getReceiver();
    LetterListDto.LetterListDtoBuilder builder = LetterListDto.builder()
        .letterId(letter.getLetterId())
        .sender(MemberSimpleDto.builder().memberId(sender.getMemberId()).memberStatus(sender.getMemberStatus())
            .name(sender.getName()).build())
        .receiver(MemberSimpleDto.builder().memberId(receiver.getMemberId()).memberStatus(receiver.getMemberStatus())
            .name(receiver.getName()).build())
        .isRead(letter.getIsRead())
        .availableAt(letter.getAvailableAt())
        .createdAt(letter.getCreatedAt())
        .body(
            LocalDateTime.now().isAfter(letter.getAvailableAt()) || sender.getMemberId() == memberId
                ? letter.getBody() : null)
        .hasPic(letter.getPhotoUrl() != null && !letter.getPhotoUrl().isEmpty());
    return builder.build();
  }
}
