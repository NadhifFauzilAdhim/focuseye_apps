package com.surendramaran.yolov8tflite.ui.news

import com.surendramaran.yolov8tflite.R

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surendramaran.yolov8tflite.data.model.Article
import com.surendramaran.yolov8tflite.databinding.ItemNewsBinding

class NewsAdapter : ListAdapter<Article, NewsAdapter.NewsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = getItem(position)
        holder.bind(article)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            // DIUBAH: Panggil NewsDetailActivity
            val intent = Intent(context, NewsDetailActivity::class.java).apply {
                putExtra("ARTICLE_URL", article.url)
                putExtra("IMAGE_URL", article.urlToImage)
                putExtra("TITLE", article.title)
            }
            context.startActivity(intent)
        }
    }

    class NewsViewHolder(private val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.textViewTitle.text = article.title
            binding.textViewSource.text = article.source?.name ?: "Sumber Tidak Diketahui"

            Glide.with(itemView.context)
                .load(article.urlToImage)
                .placeholder(R.drawable.news_no_image)
                .error(R.drawable.news_no_image)
                .into(binding.imageViewArticle)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    }
}