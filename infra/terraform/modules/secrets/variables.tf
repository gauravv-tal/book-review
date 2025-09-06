variable "name_prefix" {
  description = "Prefix for secret names, e.g., book-review"
  type        = string
}

variable "secrets_map" {
  description = "Map of secret logical names to initial string values (use empty string to create empty secret)"
  type        = map(string)
  default     = {}
}

variable "tags" {
  description = "Common tags"
  type        = map(string)
  default     = {}
}
