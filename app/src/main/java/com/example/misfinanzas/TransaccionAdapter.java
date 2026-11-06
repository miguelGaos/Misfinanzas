package com.example.misfinanzas.adaptador;

import java.text.DecimalFormat;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.misfinanzas.FormularioActivity;
import com.example.misfinanzas.R;
import com.example.misfinanzas.modelo.Transaccion;
import java.util.List;


public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.ViewHolder> {
    private List<Transaccion> listaTransacciones;

    public TransaccionAdapter(List<Transaccion> lista) {
        this.listaTransacciones = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaccion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaccion t = listaTransacciones.get(position);
        holder.tvConcepto.setText(t.getConcepto());
        java.text.DecimalFormat formato = new java.text.DecimalFormat("#,###.##");
        holder.tvMonto.setText("$ " + formato.format(t.getMonto()));
        if(t.getTipo().equals("Ingreso")) {
            holder.tvMonto.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.tvMonto.setTextColor(Color.parseColor("#F44336")); // Rojo
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FormularioActivity.class);
            intent.putExtra("id", t.getId());
            intent.putExtra("concepto", t.getConcepto());
            intent.putExtra("monto", t.getMonto());
            intent.putExtra("tipo", t.getTipo());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaTransacciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto, tvMonto;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConcepto = itemView.findViewById(R.id.tvConceptoItem);
            tvMonto = itemView.findViewById(R.id.tvMontoItem);
        }
    }
}
