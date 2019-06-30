# paired_t_test

a = c(12.9, 13.5, 12.8, 15.6, 17.2, 19.2, 12.6, 15.3, 14.4, 11.3)
b = c(12.0, 12.2, 11.2, 13.0, 15.0, 15.8, 12.2, 13.4, 12.9, 11.0)

t.test(a,b, paired=FALSE, conf.level=0.95)

df_feature_extraction <- read.table("./df_feature_extraction.csv",sep=",", header=TRUE)

t.test(df_feature_extraction)
df_feature_extraction

t_test_result = as.list(rep(0,36))
names(t_test_result) <- names(df_feature_extraction[,6:41])
for (feature_name in names(df_feature_extraction[,6:41])){
  temp <- subset(df_feature_extraction, productNo == 3, select = c(feature_name))
  temp2 <- subset(df_feature_extraction, productNo == 22, select = c(feature_name))
  res <- t.test(unlist(temp),unlist(temp2), paried=FALSE, conf.level = 0.95)$p.value
  t_test_result[[feature_name]] <- res
}

df_ttest_result <- ldply(t_test_result)

write.csv(df_ttest_result,file="./ttest_result_subject_phonenumber.csv")

df_ttest_result_legacy <- read.table("./ttest_result.csv",sep=",", header=TRUE)

mytheme <- theme_bw() + 
  theme(panel.border = element_rect(colour="black",size=1)) +
  theme(panel.grid.minor = element_blank()) +
  theme(panel.grid.major = element_line(colour="#9a9a9a",size=.2, linetype="dashed")) + 
  theme(axis.text.x = element_text(size=rel(1))) + 
  theme(axis.title.x = element_text(size=rel(1.5))) +
  theme(axis.title.y = element_text(size=rel(1.5))) +
  theme(axis.text.y = element_text(size=rel(1.5))) +
  theme(axis.text = element_text(colour = "black"))

ggplot(df_ttest_result, aes(x=.id,y=V1)) + 
  geom_bar(stat="identity", fill="white",colour="black") + 
  ylab("p-value")+ xlab("feature")+mytheme + theme(axis.text.x = element_text(angle=90)) 


unlist(t.test(
  df_feature_extraction[1,c(6:41)],
  df_feature_extraction[15,c(6:41)] , paried=TRUE, conf.level = 0.95))