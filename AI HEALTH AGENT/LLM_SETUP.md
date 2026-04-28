# SmartHealth AI Setup

The web chat calls `AIChatServlet.do`, which keeps the LLM key on the server.

Set this before starting GlassFish:

```sh
export OPENAI_API_KEY="your_openai_api_key"
```

Optional fallback if you want a SmartHealth-specific variable instead:

```sh
export SMARTHEALTH_LLM_API_KEY="your_openai_api_key"
```

Optional settings:

```sh
export SMARTHEALTH_LLM_MODEL="gpt-5"
export SMARTHEALTH_LLM_ENDPOINT="https://api.openai.com/v1/responses"
export SMARTHEALTH_AGENT_WEB_SEARCH="true"
```

If you prefer a local env file, create `.env` in the repository root and keep it out of Git:

```sh
OPENAI_API_KEY="your_openai_api_key"
SMARTHEALTH_LLM_MODEL="gpt-5"
SMARTHEALTH_AGENT_WEB_SEARCH="true"
```

Then start GlassFish with:

```sh
ruby scripts/start_glassfish_with_local_env.rb .env /Users/didintlemakhubedu/NetBeansJDKs/glassfish/bin/asadmin
```

Deploy or redeploy `AI HEALTH AGENT/dist/SWP_MergedProject2.war`, then open:

```text
http://localhost:8080/SWP_MergedProject2/healthApp.html
```

If no key is set, the app still works with a local safety fallback, but the JSON response will include `"source":"fallback_missing_key"` instead of `"source":"llm"`.

When the key is set and web search is enabled, responses include `"source":"llm_agent"`. The agent can use live web search through OpenAI's Responses API, limited to reputable health domains by default.
