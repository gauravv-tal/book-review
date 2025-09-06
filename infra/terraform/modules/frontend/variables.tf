variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "index_document" {
  description = "Default index document"
  type        = string
  default     = "index.html"
}

variable "error_document" {
  description = "Default error document"
  type        = string
  default     = "index.html" # SPA routing
}

variable "price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_100"
}

variable "tags" {
  description = "Tags to apply"
  type        = map(string)
  default     = {}
}
