(ns aws-api-stub.aws)

;; CloudFormation
(defn describe-stacks []
  {"Stacks"
   [{"StackId"
     "arn:aws:cloudformation:ap-southeast-2:337944750480:stack/DevStack-62031/1",
     "StackStatus" "CREATE_IN_PROGRESS",
     "StackName" "DevStack-62031",
     "Parameters" [{"ParameterKey" "DevDB", "ParameterValue" nil}]}]})

(defn describe-stack-resources []
  {"StackResources"
   [{"PhysicalResourceId" "EC2123",
     "ResourceType" "AWS::EC2::Instance"},
    {"PhysicalResourceId" "EC2456",
     "ResourceType" "AWS::EC2::Instance"}
    {"PhysicalResourceId" "EC2789",
     "ResourceType" "AWS::EC2::Instance"}
    {"PhysicalResourceId" "RDS123",
     "ResourceType" "AWS::RDS::DBInstance"}
    {"PhysicalResourceId" "RDS456",
     "ResourceType" "AWS::RDS::DBInstance"}]})

;; EC2
(defn describe-instances []
  {"Reservations"
   [{"Instances"
     [{"InstanceId" "EC2123",
       "Tags"
       [{"Key" "StackType", "Value" "Dev"}
        {"Key" "junkTag", "Value" "should not be included"}
        {"Key" "aws:cloudformation:logical-id", "Value" "theDude"}],
       "State" {"Name" "running"}}
      {"InstanceId" "EC2456",
       "Tags"
       [{"Key" "StackType", "Value" "Dev"}
        {"Key" "junkTag", "Value" "should not be included"}
        {"Key" "aws:cloudformation:logical-id", "Value" "theDude"}],
       "State" {"Name" "running"}}
      {"InstanceId" "EC2789",
       "Tags"
       [{"Key" "StackType", "Value" "Dev"}
        {"Key" "junkTag", "Value" "should not be included"}
        {"Key" "aws:cloudformation:logical-id", "Value" "theDude"}],
       "State" {"Name" "running"}}]}]})

;; RDS

(defn describe-db-instances []
  {"DBInstances"
   [{"DBInstanceIdentifier" "RDS123", "DBInstanceStatus" "available"}
    {"DBInstanceIdentifier" "RDS456", "DBInstanceStatus" "available"}]})