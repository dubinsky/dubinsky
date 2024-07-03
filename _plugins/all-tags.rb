module Jekyll
  module AllTagsFilter
    def all_tags(site)
      @all_tags ||= begin
       hash = Hash.new { |h, key| h[key] = [] }
       (site.documents + site.pages).each do |p|
         p.data["tags"]&.each { |t| hash[t] << p }
       end
       hash.each_value { |pages| pages.sort { |a,b| a.data["title"] <=> b.data["title"] }.reverse! }
       hash
     end
    end
  end
end

Liquid::Template.register_filter(Jekyll::AllTagsFilter)
