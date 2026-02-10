package hr.tvz.cyberpunkunfolds.docs;

import lombok.Builder;

@Builder
record DocTemplateModel(String title,
                        String headerTitle,
                        String basePackage,
                        String classCount,
                        String generatedAt,
                        String toolbar,
                        String sidebar,
                        String content) {}
