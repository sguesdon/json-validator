# json-validator
## Version: 0.0.1

### /models

#### GET
##### Summary

get model list

##### Responses

| Code | Description |
| ---- | ----------- |
| 200 | model list |
| 400 | error |

#### POST
##### Summary

create model

##### Responses

| Code | Description |
| ---- | ----------- |
| 201 | model object |
| 400 | error |

### /models/{modelId}

#### GET
##### Summary

get model by id

##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| modelId | path | model id | Yes | integer |

##### Responses

| Code | Description |
| ---- | ----------- |
| 200 | model object |
| 400 | error |

### Models

#### Model

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | No |
| name | string |  | Yes |
| tag | string |  | No |

#### Error

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| code | integer |  | Yes |
| message | string |  | Yes |
