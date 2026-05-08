package com.teacher.management.vo;

import com.teacher.management.entity.*;
import java.util.List;

/**
 * 教师成果聚合 VO — 汇总该教师名下所有历次提交的成果明细
 */
public class AchievementVO {

    private List<PaperRecord> papers;
    private List<AwardRecord> awards;
    private List<IpRecord> ips;
    private List<CompetitionRecord> competitions;
    private List<TrainingRecord> trainings;
    private List<ReportRecord> reports;
    private List<TextbookRecord> textbooks;
    private List<VerticalProject> verticalProjects;
    private List<HorizontalProject> horizontalProjects;
    private List<InnovationProject> innovationProjects;

    // --- Getters & Setters ---
    public List<PaperRecord> getPapers() { return papers; }
    public void setPapers(List<PaperRecord> papers) { this.papers = papers; }

    public List<AwardRecord> getAwards() { return awards; }
    public void setAwards(List<AwardRecord> awards) { this.awards = awards; }

    public List<IpRecord> getIps() { return ips; }
    public void setIps(List<IpRecord> ips) { this.ips = ips; }

    public List<CompetitionRecord> getCompetitions() { return competitions; }
    public void setCompetitions(List<CompetitionRecord> competitions) { this.competitions = competitions; }

    public List<TrainingRecord> getTrainings() { return trainings; }
    public void setTrainings(List<TrainingRecord> trainings) { this.trainings = trainings; }

    public List<ReportRecord> getReports() { return reports; }
    public void setReports(List<ReportRecord> reports) { this.reports = reports; }

    public List<TextbookRecord> getTextbooks() { return textbooks; }
    public void setTextbooks(List<TextbookRecord> textbooks) { this.textbooks = textbooks; }

    public List<VerticalProject> getVerticalProjects() { return verticalProjects; }
    public void setVerticalProjects(List<VerticalProject> verticalProjects) { this.verticalProjects = verticalProjects; }

    public List<HorizontalProject> getHorizontalProjects() { return horizontalProjects; }
    public void setHorizontalProjects(List<HorizontalProject> horizontalProjects) { this.horizontalProjects = horizontalProjects; }

    public List<InnovationProject> getInnovationProjects() { return innovationProjects; }
    public void setInnovationProjects(List<InnovationProject> innovationProjects) { this.innovationProjects = innovationProjects; }
}
