package com.teacher.management.dto;


import java.util.List;

/**
 * 教师提交信息聚合 DTO
 */
public class TeacherSubmitDTO {

    private String submitMonth;
    private Long taskId;
    private List<IpDTO> ipList;
    private CompetitionDTO competition;
    private TrainingDTO training;
    private ReportDTO report;
    private BookDTO book;
    private AwardDTO award;
    private List<PaperDTO> paperList;
    private ResearchProjectDTO verticalProject;
    private ResearchProjectDTO horizontalProject;
    private InnovationProjectDTO innovationProject;

    public String getSubmitMonth() { return submitMonth; }
    public void setSubmitMonth(String submitMonth) { this.submitMonth = submitMonth; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public List<IpDTO> getIpList() { return ipList; }
    public void setIpList(List<IpDTO> ipList) { this.ipList = ipList; }

    public CompetitionDTO getCompetition() { return competition; }
    public void setCompetition(CompetitionDTO competition) { this.competition = competition; }

    public TrainingDTO getTraining() { return training; }
    public void setTraining(TrainingDTO training) { this.training = training; }

    public ReportDTO getReport() { return report; }
    public void setReport(ReportDTO report) { this.report = report; }

    public BookDTO getBook() { return book; }
    public void setBook(BookDTO book) { this.book = book; }

    public AwardDTO getAward() { return award; }
    public void setAward(AwardDTO award) { this.award = award; }

    public List<PaperDTO> getPaperList() { return paperList; }
    public void setPaperList(List<PaperDTO> paperList) { this.paperList = paperList; }

    public ResearchProjectDTO getVerticalProject() { return verticalProject; }
    public void setVerticalProject(ResearchProjectDTO verticalProject) { this.verticalProject = verticalProject; }

    public ResearchProjectDTO getHorizontalProject() { return horizontalProject; }
    public void setHorizontalProject(ResearchProjectDTO horizontalProject) { this.horizontalProject = horizontalProject; }

    public InnovationProjectDTO getInnovationProject() { return innovationProject; }
    public void setInnovationProject(InnovationProjectDTO innovationProject) { this.innovationProject = innovationProject; }
}
