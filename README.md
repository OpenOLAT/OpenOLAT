
# OpenOLAT Enhancement System (CBSE Assignment)

This repository contains functional enhancements built on top of the OpenOLAT system for the CBSE assignment. The system is already using **Spring Boot** as its core framework, and all enhancements are fully integrated with the Spring Boot architecture.

Each enhancement improves an existing OpenOLAT module by addressing limitations in usability, flexibility, or analytical support.

> **Note:** OSGi modularization and runtime integration are handled in a separate repository.

---

## 📦 Enhanced Modules & Responsibilities

| Module                       | Person in Charge        |
|------------------------------|------------------------|
| Course Management Module     | Chai Li Chee           |
| Assessment Management Module | Poh Sharon             |
| Enrollment Module            | Goh Kah Kheng          |
| Scheduling Module            | Al Rubab Ibn Yeahyea   |
| Communication Module         | Eugene See Yi Le       |

---

## 🧠 Module Enhancements

### 1. Course Management Module
| Enhancement                                                  | Description                                                                                                                                                                                    |
| ------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Course Template Library with Selective Element Chooser**   | Enables course creators to preview available course templates and selectively include, exclude, or configure individual sections, elements, and learning resources *before* creating a course. |
| **Smart Course Element Duplication with Dependency Mapping** | Allows duplication of complex or hierarchical course elements while preserving parent–child relationships, configurations, resource references, and assessment logic.                          |
| **Bulk Staff Assignment**                                    | Allows administrators to assign multiple staff members to a course simultaneously, significantly reducing manual setup time and administrative overhead.                                       |
| **Course Readiness Checker**                                 | Automatically validates critical course configurations and dependencies to identify issues and ensure courses are fully prepared prior to publication.                                         |


### 2. Assessment Management Module

The following enhancements were implemented to improve assessment creation, feedback quality, and performance insights:

| Enhancement              | Description                                                                 |
|--------------------------|-----------------------------------------------------------------------------|
| **Assessment Templates** | Allows instructors to reuse predefined assessment structures across courses  |
| **Structured Feedback**  | Supports categorized and rule-based feedback linked to assessment results    |
| **Peer Review**          | Enables students to review and evaluate peer submissions                     |
| **Performance Analysis** | Provides statistical summaries of assessment results and participation       |

These enhancements reduce manual effort and improve assessment consistency without changing core system behavior.

### 3. Enrollment Module
*Details to be added...*

### 4. Scheduling Module
*Details to be added...*

### 5. Communication Module
*Details to be added...*
