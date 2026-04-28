#!/usr/bin/env ruby

require "rexml/document"

domain_xml = ARGV.fetch(0)
env_file = ARGV.fetch(1)

properties = {}
File.readlines(env_file, chomp: true).each do |line|
  next if line.strip.empty? || line.strip.start_with?("#")

  key, value = line.split("=", 2)
  next if key.nil? || value.nil?

  value = value.strip
  value = value[1..-2] if value.start_with?("'") && value.end_with?("'")
  properties[key.strip] = value
end

required_keys = ["SMARTHEALTH_DB_URL"]
missing_keys = required_keys.select { |key| properties[key].nil? || properties[key].strip.empty? }
abort("Missing required local config: #{missing_keys.join(", ")}") unless missing_keys.empty?

document = REXML::Document.new(File.read(domain_xml))
java_config = REXML::XPath.first(document, "//java-config")
abort("Could not find java-config in #{domain_xml}") if java_config.nil?

def escape_jvm_option(value)
  value.to_s.strip.gsub(/(?<!\\):/, "\\:")
end

REXML::XPath.each(java_config, "jvm-options") do |option|
  option.text = escape_jvm_option(option.text)
end

properties.each do |key, value|
  next unless key.start_with?("SMARTHEALTH_") || key.start_with?("SUPABASE_")

  option_prefix = "-D#{key}="
  REXML::XPath.each(java_config, "jvm-options") do |option|
    java_config.delete_element(option) if option.text && option.text.start_with?(option_prefix)
  end

  option = java_config.add_element("jvm-options")
  option.text = escape_jvm_option("#{option_prefix}#{value}")
end

File.open(domain_xml, "w") do |file|
  document.write(file, 0)
end

puts "GlassFish domain updated with local SmartHealth configuration."
