package org.le.core;

import org.apache.commons.lang3.StringUtils;
import org.le.Exception.BigPipeJsPreHandleException;

import java.util.Arrays;
import java.util.List;

public class SimpleBigpipeSupport implements BigpipeSupportStrategy {

    private static SimpleBigpipeSupport instance = new SimpleBigpipeSupport();

    private SimpleBigpipeSupport() {
    }

    public static SimpleBigpipeSupport newInstance() {
        return instance;
    }

    private static final String bigpipeScript = "<script type=\"application/javascript\">\n" +
            "        function replace(id, content) {\n" +
            "            var pagelet = document.getElementById(id);\n" +
            "            pagelet.innerHTML = content;\n" +
            "        }\n" +
            "    </script>";

    @Override
    public String execute(String html) {
        List<String> tags = Arrays.asList("</head>", "</title>");
        String result = null;
        for (String tag : tags) {
            result = build(html, tag);
            if (StringUtils.isNotEmpty(result)) {
                break;
            }
        }
        if (StringUtils.isEmpty(result)) {
            StringBuilder sb = new StringBuilder();
            for (String tag : tags)
                sb.append(tag).append(",");
            throw new BigPipeJsPreHandleException("html must contain these" + sb.toString() + "tags");
        } else
            return result;
    }

    private String build(String html, String tag) {
        StringBuilder sb = new StringBuilder();
        int indexOfHeadEnd = StringUtils.indexOfIgnoreCase(html, tag);
        if (indexOfHeadEnd > 0) {
            sb.append(html.substring(0, indexOfHeadEnd))
                    .append(bigpipeScript)
                    .append("\n")
                    .append(html.substring(indexOfHeadEnd, html.length()));
        }
        return sb.toString();
    }
}
