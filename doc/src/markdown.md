# Markdown Extensions

The following extensions are provided out of the box. Except for `autolink` they are all enabled by default.

```yaml src=../../core/src/main/resources/ornate-reference.conf#--doc-extension-aliases
```

## commonmark-java extensions

See the [commonmark-java documentation](https://github.com/atlassian/commonmark-java#extensions) for `autolink`, `strikethrough` and `tables`.

## `headerAttributes` and `autoIdentifiers` {#header_attributes}

The `headerAttributes` extension implements the same header attribute syntax as [Pandoc](http://pandoc.org/MANUAL.html#header-identifiers) and [PHP Markdown Extra](https://michelf.ca/projects/php-markdown/extra/). Only the IDs are used by Ornate. Classes and key/value pairs are currently ignored. This extension is essential for controlling header links. Every heading to which you want to link from other parts of the document, from the TOC, or from external sources, requires an ID. In case of the TOC, headings without an ID will still be listed but not linked.

Here is an example for a heading title with an ID:

```markdown
## All Configuration Settings {#settings}

Links to this section get to use `#settings` instead of
`#all_configuration_settings` (the auto-generated ID).
```

The `autoIdentifiers` extension uses the same algorithm as Pandoc to automatically derive an ID from the heading title if no ID was set explicitly via `headerAttributes`

## `blockQuoteAttributes`

This extension implements the same header attribute syntax for block quotes. Ornate recogizes the classes `.note` and `.warning` in block quote attributes to generate appropriately styled note and warning blocks. The header attributes must be the only thing on the first line of the block quote. Content starts on the second line.

Example source:

```markdown
> This is a reqular block quote.

> {.note}
> This is a note.

> {.warning}
> This is a warning.
```

This gets rendered as:

> This is a reqular block quote.

> {.note}
> This is a note.

> {.warning}
> This is a warning.

## `mergeTabs`

This extension allows you to merge directly adjacent fenced code blocks into a tabbed view. This is controlled through the *info string* of the fenced code block. The CommonMark specification leaves interpretation of this string undefined, suggesting only that the first token define the highlighting language. Ornate parses the info string with the same syntax as [header attributes](#header_attributes). Adjacent fenced code blocks are merged if their info string contains a key/value pair with the key `tab`. The value is used as the tab title.

Example source:

````markdown
```scala tab=Scala
class AttributedHeading extends Heading {
  // ...
}
```

```java tab="Java Version"
public class AttributedHeading extends Heading {
  // ...
}
```
````

This gets rendered as:

```scala tab=Scala
class AttributedHeading extends Heading {
  // ...
}
```

```java tab="Java Version"
public class AttributedHeading extends Heading {
  // ...
}
```

## `includeCode`

This extension allows you to include code snippets from an external file in a fenced code block. The `src` attribute specifies the external file relative to the source URI of the current page. If the URI has a fragment, it is used to extract only parts the file delimited by lines ending with the fragment ID (including the `#` symbol). The delimiter lines are not included, only the lines between them. Multiple delimited sections are allowed. They are concatenated when extrating the snippet. Each section is dedented individually by stripping off leading whitespace that is common to all lines (*including* the delimiter lines).

If the fenced code block is not empty, its original content is discarded. It can be used to show a placeholder in Markdown processors without this `includeCode` feature.

Example source:

````markdown
```scala src=../../core/src/main/scala/com/novocode/ornate/Main.scala#main
  Snippet Placeholder
```
````

This gets rendered as:

```scala src=../../core/src/main/scala/com/novocode/ornate/Main.scala#main
  Snippet Placeholder
```

## `expandVars`

This extension allows expansion of variables that reference configuration keys. The delimiters for variables are configurable, the default style being `{{variable}}`. Variable substutions are performed *after* Markdown parsing, so there is no way to escape delimiters. Global expansion for different node types can also be enabled in the configuration. By default this extension is enabled but all expansion options are disabled, so expansion is only performed in fenced code blocks with an explicit `expandVars=true` attribute. This is the default configuration:

```yaml src=../../core/src/main/resources/ornate-reference.conf#--doc-expandVars
```

Example source:

````markdown
```yaml expandVars=true
# Set the version to {{version}}:
version = "{{version}}"
```
````

This gets rendered as:

```yaml expandVars=true
# Set the version to {{version}}:
version = "{{version}}"
```

> {.note}
> Note: When using `expandVars` together with `includeCode`, the order in which the extensions are run determines whether variables in included snippets are expanded. The default configuration adds `expandVars` *after* `includeCode` so that snippets are processed by `expandVars`.

In plain text you can also use objects with [config](images.md#config) URIs instead of `expandVars` but this is not possible in code elements, which cannot contain embedded Markdown syntax.

## `emoji`

The `emoji` extension translates Emoji names to the appropriate Unicode representations or images.

Example source:

```markdown
Is this feature :thumbsup: or :thumbsdown:?
```

This gets rendered as:

Is this feature :thumbsup: or :thumbsdown:?

## `globalRefs`

This extension allows you to prepend reference targets that are defined in the site config or page config to every page. This is useful for targets that are either computed from other config values or used on many pages. This is the default configuration:

```yaml src=../../core/src/main/resources/ornate-reference.conf#--doc-globalRefs
```

The keys in the map are the reference labels. The values are defined in the same way as [TOC entries](configuration.md#table-of-contents), either as a string (containing the link target) or an object with the fields `url` and `title`.
