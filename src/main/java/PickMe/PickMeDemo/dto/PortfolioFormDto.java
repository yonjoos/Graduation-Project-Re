package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioFormDto {

    private Boolean hasPortfolio;
    private Integer web;
    private Integer app;
    private Integer game;
    private Integer ai;
    private String shortIntroduce;
    private String introduce;
    private List<MultipartFile> promoteImageUrl; // 이미지 파일 자체를 리스트로 받음
    private List<MultipartFile> fileUrl; // 첨부파일 자체를 리스트로 받음
}
