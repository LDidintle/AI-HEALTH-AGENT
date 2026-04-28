#!/usr/bin/env ruby

env_file = ARGV.fetch(0)
asadmin = ARGV.fetch(1)
domain = ARGV.fetch(2, "domain1")

File.readlines(env_file, chomp: true).each do |line|
  next if line.strip.empty? || line.strip.start_with?("#")

  key, value = line.split("=", 2)
  next if key.nil? || value.nil?

  value = value.strip
  value = value[1..-2] if value.start_with?("'") && value.end_with?("'")
  ENV[key.strip] = value
end

exec(asadmin, "start-domain", "--verbose", domain)
