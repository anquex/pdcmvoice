% absolute error
clear all
close all
hold on;
hold all;
Ts=[30 20 10];

for T=Ts
    load(['GMLE-bias-T' int2str(T)]);
    load ('biasT');
    avg_bias=avg_biasT(T);
    if (avg_bias==0)
        error('probabile errore, bias==0');
    end
    
    numbers=1:size(D,2);
    abs_biased_err=zeros(1,length(batchsizes));
    abs_unbiased_err=zeros(1,length(batchsizes));
    for k=1:length(batchsizes)
        n=batchsizes(k)
        abs_biased_err(k)=(sum(abs(numbers-n).*D(k,:)))/n;
        abs_unbiased_err(k)=(sum(abs(numbers/(avg_bias+1)-n).*D(k,:)))/n;
    end
    clear D GMLE 
    plot(batchsizes,abs_biased_err);
    plot(batchsizes,abs_unbiased_err);
end

clear D GMLE 
T=100;
load(['GMLE-bias-T' int2str(T)]);
numbers=1:size(D,2);
avg_bias=avg_biasT(T);
abs_biased_err=zeros(1,length(batchsizes));
for k=1:length(batchsizes)
    n=batchsizes(k)
    abs_biased_err(k)=(sum(abs(numbers-n).*D(k,:)))/n;
    abs_unbiased_err(k)=(sum(abs(numbers/(avg_bias+1)-n).*D(k,:)))/n;
end
plot(batchsizes,abs_biased_err);
plot(batchsizes,abs_unbiased_err);


set(gca,'YTick',[0:0.01:1])
grid on;
hy=ylabel('Expected Normalized Absolute Error','Interpreter','Latex');
hx=xlabel('$n$','Interpreter','Latex');
set(hx, 'FontSize', 11);
set(hy, 'FontSize', 11);
legend('$T=30$, $\hat{n}$','$T=30$, $\tilde{n}$','$T=10$, $\hat{n}$','$T=10$, $\tilde{n}$','$T=20$, $\hat{n}$','$T=20$, $\tilde{n}$','$T=100$, $\hat{n}$','$T=100$, $\tilde{n}$','Location','BestOutside');
hhl = legend;
set(hhl, 'Interpreter', 'Latex');
set(hhl, 'FontSize', 11);
set(gca,'YLim',[0.07 0.25])
% fh=figure(1);
% saveas(fh,'GEGA-bias-unbias-absolute-normalized-error', 'epsc');