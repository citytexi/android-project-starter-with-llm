# R8 rules that apps using :core:network need.
#
# Empty on purpose. kotlinx.serialization (META-INF/com.android.tools/r8/*.pro), Retrofit and
# OkHttp ship their own rules, and those already keep what the @Serializable models in
# `model/` need: the `Companion` field, `serializer()` and the generated `$$serializer`.
# Add a rule here only for something those rules miss, with a comment saying why.
