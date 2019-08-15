library(shiny)
library(ggplot2)
library(ggvis)
library(reshape)
library(gridExtra)

#for Large Data
options("scipen"=100)
options(digits = 13)

source("functions.R")

#ui.R is for UI
#user input to the each inputPanel is passed to server.R
fluidPage(
  fluidRow(
    column(6,column(11, div(style="heigth:50px;",h2('Graph')),offset = 1),offset=1),
    column(2,
           fluidRow(
             
             #about mode (default, customize, data set)
             radioButtons(inputId = "mode",label = NULL, choices = c("default","customize","Data Set"),inline = T),
             conditionalPanel("input.mode=='Data Set'",radioButtons(inputId = "dataMode",label = NULL, choices = c("Phone","E4"),inline = T)) # if 'data set' mode, must select phone or e4 mode 
           )
           ,offset = 3)
  ),
  fluidRow(
    column(3,
           fluidRow(
             #user selection
             column(8,wellPanel(id="tPanel",style="overflow-y:scroll;max-height:150px;",
                                checkboxGroupInput("selectFile_check","Select File",choices = usersName,selected = usersName)),offset = 1),
             column(3)),
           fluidRow(column(11,
                           #the input panel is must be separately for each mode
                           #to pass the input properly
                           conditionalPanel("input.mode=='default'",
                                            div(style="heigth:200px;",
                                                fluidRow(
                                                  selectInput(inputId = "selectGraph",label = "Graph Type", choices = c("Accelerometer","Gyroscope","Location","Temperature","Inter Beat Interval","Blood Volume Pulse","GSR"), selected = "Location"),
                                                  conditionalPanel("input.selectGraph!='Location'",selectInput(inputId = "selectUser",label = "User Num",choices = users, selected = users[1])),
                                                  dateRangeInput(inputId = "selectDate",label = "Date",start = Sys.Date(), end = Sys.Date(),format = "yyyy-mm-dd")
                                                ),
                                                fluidRow(
                                                  h5('Start Time'),
                                                  div(style="height:20px; font-size:9px;",
                                                      column(3, numericInput(inputId = "selectHour_s",label = "Hour",min = 0, max = 23,value = 0,step = 1)),
                                                      column(3, numericInput(inputId = "selectMin_s",label = "Min",min = 0, max = 59,value = 0,step=1)),
                                                      column(3, numericInput(inputId = "selectSec_s",label = "Sec",min = 0, max = 59, value = 0,step=1)),
                                                      column(1))
                                                ),
                                                fluidRow(
                                                  h5('End Time'),
                                                  div(style="height:20px; font-size:9px;",
                                                      
                                                      column(3, numericInput(inputId = "selectHour_e",label = "Hour",min = 0, max = 23,value = 23,step = 1)),
                                                      column(3, numericInput(inputId = "selectMin_e",label = "Min",min = 0, max = 59, value = 59,step=1)),
                                                      column(3, numericInput(inputId = "selectSec_e",label = "Sec",min = 0, max = 59, value = 59,step=1)),
                                                      column(1)
                                                  )
                                                ),
                                                fluidRow(
                                                  conditionalPanel("input.selectGraph == 'Location'",
                                                                   sliderInput(inputId = "selectLa",label = "Latitude",min = 0, max = 90,value = c(0,90),step = 1),
                                                                   sliderInput(inputId = "selectLo",label = "Longitude",min = 0, max = 180,value = c(0,180),step = 1))
                                                )
                                                
                                            )),
                           conditionalPanel("input.mode=='Data Set'",
                                            div(style="heigth:200px;",
                                                conditionalPanel("input.dataMode=='Phone'",
                                                                 fluidRow(
                                                                   checkboxGroupInput(inputId ="selectData2", label = NULL, choices = c("Accelerometer","Gyroscope","Location"), selected =  c("Accelerometer","Gyroscope","Location")),
                                                                   
                                                                   selectInput(inputId = "selectUser2",label = "User Num",choices = users, selected = users[1]),
                                                                   dateRangeInput(inputId = "selectDate2",label = "Date",start = Sys.Date(), end = Sys.Date(),format = "yyyy-mm-dd")
                                                                 ),
                                                                 fluidRow(
                                                                   h5('Start Time'),
                                                                   div(style="height:20px; font-size:9px;",
                                                                       column(3, numericInput(inputId = "selectHour_s2",label = "Hour",min = 0, max = 23,value = 0,step = 1)),
                                                                       column(3, numericInput(inputId = "selectMin_s2",label = "Min",min = 0, max = 59,value = 0,step=1)),
                                                                       column(3, numericInput(inputId = "selectSec_s2",label = "Sec",min = 0, max = 59, value = 0,step=1)),
                                                                       column(1))
                                                                 ),
                                                                 fluidRow(
                                                                   h5('End Time'),
                                                                   div(style="height:20px;font-size:9px;",
                                                                       column(3, numericInput(inputId = "selectHour_e2",label = "Hour",min = 0, max = 23,value = 23,step = 1)),
                                                                       column(3, numericInput(inputId = "selectMin_e2",label = "Min",min = 0, max = 59, value = 59,step=1)),
                                                                       column(3, numericInput(inputId = "selectSec_e2",label = "Sec",min = 0, max = 59, value = 59,step=1)),
                                                                       column(1)
                                                                   )
                                                                 ),
                                                                 fluidRow(
                                                                   sliderInput(inputId = "selectLa2",label = "Latitude",min = 0, max = 90,value = c(0,90),step = 1),
                                                                   sliderInput(inputId = "selectLo2",label = "Longitude",min = 0, max = 180,value = c(0,180),step = 1)
                                                                 )),
                                                conditionalPanel("input.dataMode=='E4'",
                                                                 fluidRow(
                                                                   checkboxGroupInput(inputId ="selectData2_1", label = NULL, choices = c("Accelerometer","Temperature","Inter Beat Interval","Blood Volume Pulse","GSR"),selected =  c("Accelerometer","Temperature","Inter Beat Interval","Blood Volume Pulse","GSR")),
                                                                   
                                                                   selectInput(inputId = "selectUser2_1",label = "User Num",choices = users, selected = users[1]),
                                                                   dateRangeInput(inputId = "selectDate2_1",label = "Date",start = Sys.Date(), end = Sys.Date(),format = "yyyy-mm-dd")
                                                                 ),
                                                                 fluidRow(
                                                                   h5('Start Time'),
                                                                   div(style="height:20px;font-size:9px;",
                                                                       column(3, numericInput(inputId = "selectHour_s2_1",label = "Hour",min = 0, max = 23,value = 0,step = 1)),
                                                                       column(3, numericInput(inputId = "selectMin_s2_1",label = "Min",min = 0, max = 59,value = 0,step=1)),
                                                                       column(3, numericInput(inputId = "selectSec_s2_1",label = "Sec",min = 0, max = 59, value = 0,step=1)),
                                                                       column(1))
                                                                 ),
                                                                 fluidRow(
                                                                   h5('End Time'),
                                                                   div(style="height:20px;font-size:9px;",
                                                                       column(3, numericInput(inputId = "selectHour_e2_1",label = "Hour",min = 0, max = 23,value = 23,step = 1)),
                                                                       column(3, numericInput(inputId = "selectMin_e2_1",label = "Min",min = 0, max = 59, value = 59,step=1)),
                                                                       column(3, numericInput(inputId = "selectSec_e2_1",label = "Sec",min = 0, max = 59, value = 59,step=1)),
                                                                       column(1)
                                                                   )
                                                                 ),
                                                                 fluidRow(
                                                                   sliderInput(inputId = "selectLa2_1",label = "Latitude",min = 0, max = 90,value = c(0,90),step = 1),
                                                                   sliderInput(inputId = "selectLo2_1",label = "Longitude",min = 0, max = 180,value = c(0,180),step = 1)
                                                                 ))
                                                
                                            )),
                           conditionalPanel("input.mode=='customize'",
                                            #in customize mode, select multi or individual mode
                                            radioButtons(inputId = "userMode", label = NULL, choices = c("Multi","Individual"),selected = "Multi"),
                                            conditionalPanel("input.userMode=='Individual'",
                                                             selectInput(inputId = "selectUser3_Individual",label = "Users",choices = users, selected = users[1]),
                                                             dateRangeInput(inputId = "selectDate3_1",label = "Date",start = Sys.Date(), end = Sys.Date(),format = "yyyy-mm-dd"),
                                                             conditionalPanel("input.Y_Axis!='NULL'",selectInput(inputId = "selectGraphType_1",label = "Graph Type", choices = c("line","step","point","smooth","text","label","jitter","rug"))),
                                                             conditionalPanel("input.Y_Axis=='NULL'",selectInput(inputId = "selectGraphType2_1",label = "Graph Type", choices = c("area","density","dotplot","freqpoly","histogram"))),
                                                             
                                                             fluidRow(
                                                               h5('Start Time'),
                                                               div(style="height:20px;font-size:9px;",
                                                                   column(3, numericInput(inputId = "selectHour_s3_1",label = "Hour",min = 0, max = 23,value = 0,step = 1)),
                                                                   column(3, numericInput(inputId = "selectMin_s3_1",label = "Min",min = 0, max = 59,value = 0,step=1)),
                                                                   column(3, numericInput(inputId = "selectSec_s3_1",label = "Sec",min = 0, max = 59, value = 0,step=1)),
                                                                   column(1))
                                                             ),
                                                             fluidRow(
                                                               h5('End Time'),
                                                               div(style="height:20px;font-size:9px;",
                                                                   column(3, numericInput(inputId = "selectHour_e3_1",label = "Hour",min = 0, max = 23,value = 23,step = 1)),
                                                                   column(3, numericInput(inputId = "selectMin_e3_1",label = "Min",min = 0, max = 59, value = 59,step=1)),
                                                                   column(3, numericInput(inputId = "selectSec_e3_1",label = "Sec",min = 0, max = 59, value = 59,step=1)),
                                                                   column(1)
                                                               )
                                                             ),
                                                             fluidRow(
                                                               conditionalPanel("input.selectGraph3 == 'Location'",
                                                                                sliderInput(inputId = "selectLa3_1",label = "Latitude",min = 0, max = 90,value = c(0,90),step = 1),
                                                                                sliderInput(inputId = "selectLo3_1",label = "Longitude",min = 0, max = 180,value = c(0,180),step = 1)
                                                               )
                                                             )
                                            ),
                                            conditionalPanel("input.userMode=='Multi'",
                                                             checkboxGroupInput(inputId = "selectUser3_multi",label = "Users",choices = users, selected = users),
                                                             dateRangeInput(inputId = "selectDate3",label = "Date",start = Sys.Date(), end = Sys.Date(),format = "yyyy-mm-dd"),
                                                             conditionalPanel("input.Y_Axis!='NULL'",selectInput(inputId = "selectGraphType",label = "Graph Type", choices = c("line","step","point","smooth","text","label","jitter","rug"))),
                                                             conditionalPanel("input.Y_Axis=='NULL'",selectInput(inputId = "selectGraphType2",label = "Graph Type", choices = c("area","density","dotplot","freqpoly","histogram"))),
                                                             
                                                             fluidRow(
                                                               h5('Start Time'),
                                                               div(style="height:20px;font-size:9px;",
                                                                   column(3, numericInput(inputId = "selectHour_s3",label = "Hour",min = 0, max = 23,value = 0,step = 1)),
                                                                   column(3, numericInput(inputId = "selectMin_s3",label = "Min",min = 0, max = 59,value = 0,step=1)),
                                                                   column(3, numericInput(inputId = "selectSec_s3",label = "Sec",min = 0, max = 59, value = 0,step=1)),
                                                                   column(1))
                                                             ),
                                                             fluidRow(
                                                               h5('End Time'),
                                                               div(style="height:20px;font-size:9px;",
                                                                   column(3, numericInput(inputId = "selectHour_e3",label = "Hour",min = 0, max = 23,value = 23,step = 1)),
                                                                   column(3, numericInput(inputId = "selectMin_e3",label = "Min",min = 0, max = 59, value = 59,step=1)),
                                                                   column(3, numericInput(inputId = "selectSec_e3",label = "Sec",min = 0, max = 59, value = 59,step=1)),
                                                                   column(1)
                                                               )
                                                             ),
                                                             fluidRow(
                                                               conditionalPanel("input.selectGraph3 == 'Location'",
                                                                                sliderInput(inputId = "selectLa3",label = "Latitude",min = 0, max = 90,value = c(0,90),step = 1),
                                                                                sliderInput(inputId = "selectLo3",label = "Longitude",min = 0, max = 180,value = c(0,180),step = 1)
                                                               )
                                                             )
                                            )
                                            
                           )
                           ,offset = 1)),
           
           offset = 0),
    column(8,
           #when in default,customize mode, output is plot
           #in data set mode, output is datatable
           conditionalPanel("input.mode=='default'", div(style="heigth:800px;",plotOutput("graph"))),
           conditionalPanel("input.mode=='Data Set'",
                            conditionalPanel("input.dataMode=='Phone'",dataTableOutput(outputId = "dataTable")),
                            conditionalPanel("input.dataMode=='E4'",dataTableOutput(outputId = "e4dataTable"))
           ),
           conditionalPanel("input.mode=='customize'",
                            fluidRow(
                              column(5,
                                     selectInput(inputId = "X_Axis",label = "X-Axis",choices = inputXAxis,selected = "Time")),
                              column(5,
                                     selectInput(inputId = "Y_Axis",label = "Y-Axis",choices = inputYAxis,selected = "NULL"))
                            ),
                            fluidRow(
                              conditionalPanel("input.userMode=='Multi'",plotOutput("customize_graph")),
                              conditionalPanel("input.userMode=='Individual'",plotOutput("customize_graph_Indi"))
                            )
           )
    )
  )
  
  
)

